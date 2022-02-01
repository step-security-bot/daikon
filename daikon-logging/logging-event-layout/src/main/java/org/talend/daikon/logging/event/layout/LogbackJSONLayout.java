package org.talend.daikon.logging.event.layout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Marker;
import org.talend.daikon.logging.ecs.EcsFieldsMarker;
import org.talend.daikon.logging.ecs.EcsSerializer;
import org.talend.daikon.logging.event.field.HostData;

import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.LayoutBase;
import co.elastic.logging.AdditionalField;
import co.elastic.logging.EcsJsonSerializer;

/**
 * Logback ECS JSON layout
 */
public class LogbackJSONLayout extends LayoutBase<ILoggingEvent> {

    private boolean locationInfo;

    private boolean hostInfo;

    private boolean addEventUuid;

    /**
     * Legacy mode allows non-ECS fields (used for daikon-audit).
     */
    private boolean legacyMode;

    private ThrowableProxyConverter throwableProxyConverter;

    private String serviceName;

    /**
     * (Legacy-Audit)
     * A map between MDC keys and field names in the output json.
     *
     * For example,
     * metaFields.put("talend.meta.application", "application");
     *
     * Then if MDC contains an entry with "talend.meta.application=some-app",
     * it will put "application": "some-app" in the resulting json.
     */
    private final Map<String, String> metaFields = new LinkedHashMap<>();

    private final List<AdditionalField> additionalFields = new ArrayList<>();

    /**
     * Print no location info by default, but print host information (for backward compatibility).
     */
    public LogbackJSONLayout() {
        this(false, true, true);
    }

    /**
     * Creates a layout that optionally inserts location information into log messages.
     *
     * @param locationInfo whether or not to include location information in the log messages.
     */
    public LogbackJSONLayout(final boolean locationInfo, final boolean hostInfo, final boolean addEventUuid) {
        this.locationInfo = locationInfo;
        this.hostInfo = hostInfo;
        this.addEventUuid = addEventUuid;
    }

    public boolean isLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(final boolean locationInfo) {
        this.locationInfo = locationInfo;
    }

    public boolean isHostInfo() {
        return hostInfo;
    }

    public void setHostInfo(final boolean hostInfo) {
        this.hostInfo = hostInfo;
    }

    public boolean isAddEventUuid() {
        return addEventUuid;
    }

    public void setAddEventUuid(final boolean addEventUuid) {
        this.addEventUuid = addEventUuid;
    }

    public boolean isLegacyMode() {
        return legacyMode;
    }

    public void setLegacyMode(final boolean legacyMode) {
        this.legacyMode = legacyMode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(final String serviceName) {
        this.serviceName = serviceName;
    }

    public void addAdditionalField(final AdditionalField pair) {
        this.additionalFields.add(pair);
    }

    public void setMetaFields(final Map<String, String> metaFields) {
        this.metaFields.clear();
        if (metaFields != null) {
            this.metaFields.putAll(metaFields);
        }
    }

    @Override
    public void start() {
        super.start();
        throwableProxyConverter = new ThrowableProxyConverter();
        throwableProxyConverter.start();
    }

    @Override
    public String doLayout(final ILoggingEvent event) {
        final StringBuilder builder = new StringBuilder();
        EcsJsonSerializer.serializeObjectStart(builder, event.getTimeStamp());
        EcsJsonSerializer.serializeLogLevel(builder, event.getLevel().toString());
        EcsJsonSerializer.serializeFormattedMessage(builder, event.getFormattedMessage());
        EcsSerializer.serializeEcsVersion(builder);
        serializeMarkers(builder, event);
        EcsJsonSerializer.serializeServiceName(builder, serviceName);
        EcsJsonSerializer.serializeThreadName(builder, event.getThreadName());
        EcsJsonSerializer.serializeLoggerName(builder, event.getLoggerName());

        // Serialize custom markers with format key:value
        serializeCustomMarkers(builder, event);

        // Call custom serializer for additional fields & MDC (for mapping and filtering)
        EcsSerializer.serializeAdditionalFields(builder, additionalFields);
        EcsSerializer.serializeMDC(builder, event.getMDCPropertyMap(), metaFields, legacyMode);

        if (this.hostInfo) {
            EcsSerializer.serializeHostInfo(builder, new HostData());
        }

        if (this.addEventUuid) {
            EcsSerializer.serializeEventId(builder, UUID.randomUUID());
        }

        if (this.locationInfo) {
            final StackTraceElement[] callerData = event.getCallerData();
            if (callerData != null && callerData.length > 0) {
                EcsJsonSerializer.serializeOrigin(builder, callerData[0]);
            }
        }
        final IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy instanceof ThrowableProxy) {
            EcsJsonSerializer.serializeException(builder, ((ThrowableProxy) throwableProxy).getThrowable(), false);
        } else if (throwableProxy != null) {
            EcsJsonSerializer.serializeException(builder, throwableProxy.getClassName(), throwableProxy.getMessage(),
                    throwableProxyConverter.convert(event), false);
        }
        EcsJsonSerializer.serializeObjectEnd(builder);
        return builder.toString();
    }

    private void serializeMarkers(final StringBuilder builder, final ILoggingEvent event) {
        final Marker marker = event.getMarker();
        if (marker != null) {
            serializeMarkerEcsFields(builder, marker);
            serializeMarkerTags(builder, marker);
        }
    }

    private void serializeMarkerEcsFields(final StringBuilder builder, final Marker marker) {
        if (marker != null) {
            if (EcsFieldsMarker.ECS_FIELDS_MARKER_NAME.equals(marker.getName())) {
                EcsSerializer.serializeEcsFieldsMarker(builder, (EcsFieldsMarker) marker);
            } else {
                final Iterator<Marker> it = marker.iterator();
                while (it.hasNext()) {
                    final Marker currentMarker = it.next();
                    if (EcsFieldsMarker.ECS_FIELDS_MARKER_NAME.equals(currentMarker.getName())) {
                        EcsSerializer.serializeEcsFieldsMarker(builder, (EcsFieldsMarker) currentMarker);
                    }
                }
            }
        }
    }

    private void serializeMarkerTags(final StringBuilder builder, final Marker marker) {
        if (marker != null) {
            final boolean hasTags = !EcsFieldsMarker.ECS_FIELDS_MARKER_NAME.equals(marker.getName()) || marker.hasReferences();
            if (hasTags) {
                EcsJsonSerializer.serializeTagStart(builder);
                serializeMarkerTag(marker, builder);
                EcsJsonSerializer.serializeTagEnd(builder);
            }
        }
    }

    private void serializeMarkerTag(final Marker marker, final StringBuilder builder) {
        if (marker != null && !EcsFieldsMarker.ECS_FIELDS_MARKER_NAME.equals(marker.getName())) {
            EcsJsonSerializer.serializeSingleTag(builder, marker.getName());
            final Iterator<Marker> it = marker.iterator();
            while (it.hasNext()) {
                serializeMarkerTag(it.next(), builder);
            }
        }
    }

    private void serializeCustomMarkers(final StringBuilder builder, final ILoggingEvent event) {
        final Marker marker = event.getMarker();
        if (marker != null) {
            serializeCustomMarker(marker, builder);
        }
    }

    private void serializeCustomMarker(final Marker marker, final StringBuilder builder) {
        if (marker != null) {
            EcsSerializer.serializeCustomMarker(builder, marker.getName());
            final Iterator<Marker> it = marker.iterator();
            while (it.hasNext()) {
                serializeCustomMarker(it.next(), builder);
            }
        }
    }
}
