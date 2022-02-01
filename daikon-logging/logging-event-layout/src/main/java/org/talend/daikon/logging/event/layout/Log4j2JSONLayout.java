package org.talend.daikon.logging.event.layout;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.core.layout.Encoder;
import org.apache.logging.log4j.core.util.KeyValuePair;
import org.talend.daikon.logging.ecs.EcsFieldsMarker;
import org.talend.daikon.logging.ecs.EcsSerializer;
import org.talend.daikon.logging.event.field.HostData;

import co.elastic.logging.AdditionalField;
import co.elastic.logging.EcsJsonSerializer;
import co.elastic.logging.JsonUtils;

/**
 * Log4j2 ECS JSON layout
 */
@Plugin(name = "Log4j2ECSLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE, printObject = true)
public class Log4j2JSONLayout extends AbstractStringLayout {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    private final List<AdditionalField> additionalFields;

    private final String serviceName;

    private final boolean locationInfo;

    private final boolean hostInfo;

    private final boolean addEventUuid;

    private Log4j2JSONLayout(final Configuration config, final String serviceName, final boolean locationInfo,
            final boolean hostInfo, final boolean addEventUuid, final KeyValuePair[] additionalFields) {
        super(config, UTF_8, null, null);
        this.serviceName = serviceName;
        this.locationInfo = locationInfo;
        this.hostInfo = hostInfo;
        this.addEventUuid = addEventUuid;
        this.additionalFields = Stream.of(additionalFields).map(p -> new AdditionalField(p.getKey(), p.getValue()))
                .collect(Collectors.toList());
    }

    @PluginBuilderFactory
    public static Log4j2JSONLayout.Builder newBuilder() {
        return new Log4j2JSONLayout.Builder();
    }

    @Override
    public String toSerializable(final LogEvent event) {
        final StringBuilder text = toText(event, getStringBuilder());
        return text.toString();
    }

    @Override
    public void encode(final LogEvent event, final ByteBufferDestination destination) {
        final StringBuilder text = toText(event, getStringBuilder());
        final Encoder<StringBuilder> helper = getStringBuilderEncoder();
        helper.encode(text, destination);
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    private StringBuilder toText(final LogEvent event, final StringBuilder builder) {
        EcsJsonSerializer.serializeObjectStart(builder, event.getTimeMillis());
        EcsJsonSerializer.serializeLogLevel(builder, event.getLevel().toString());
        EcsJsonSerializer.serializeFormattedMessage(builder, event.getMessage().getFormattedMessage());
        EcsSerializer.serializeEcsVersion(builder);
        EcsJsonSerializer.serializeServiceName(builder, serviceName);
        EcsJsonSerializer.serializeThreadName(builder, event.getThreadName());
        EcsJsonSerializer.serializeLoggerName(builder, event.getLoggerName());

        serializeMarkers(builder, event);

        // Serialize custom markers with format key:value
        serializeCustomMarkers(builder, event.getMarker());

        // Call custom serializer for additional fields & MDC (for mapping and filtering)
        EcsSerializer.serializeAdditionalFields(builder, additionalFields);
        EcsSerializer.serializeMDC(builder, event.getContextData().toMap());

        if (this.hostInfo) {
            EcsSerializer.serializeHostInfo(builder, new HostData());
        }

        if (this.addEventUuid) {
            EcsSerializer.serializeEventId(builder, UUID.randomUUID());
        }

        if (locationInfo) {
            EcsJsonSerializer.serializeOrigin(builder, event.getSource());
        }
        EcsJsonSerializer.serializeException(builder, event.getThrown(), false);
        EcsJsonSerializer.serializeObjectEnd(builder);
        return builder;
    }

    private void serializeMarkers(final StringBuilder builder, final LogEvent event) {
        if (event != null) {
            serializeMarkerEcsFields(event.getMarker(), builder);
            serializeMarkerTags(event, builder);
        }
    }

    private void serializeMarkerEcsFields(final Marker marker, final StringBuilder builder) {
        if (marker != null) {
            if (EcsFieldsMarker.ECS_FIELDS_MARKER_NAME.equals(marker.getName())) {
                EcsSerializer.serializeEcsFieldsMarker(builder, (EcsFieldsMarker) marker);
            } else {
                final Marker[] parents = marker.getParents();
                final Optional<Marker> ecsFieldsMarker = Arrays.stream(parents)
                        .filter(parent -> EcsFieldsMarker.ECS_FIELDS_MARKER_NAME.equals(parent.getName())).findFirst();
                ecsFieldsMarker.ifPresent(value -> EcsSerializer.serializeEcsFieldsMarker(builder, (EcsFieldsMarker) value));
            }
        }
    }

    private void serializeMarkerTags(final LogEvent event, final StringBuilder builder) {
        final ThreadContext.ContextStack stack = event.getContextStack();
        final List<String> contextStack;
        if (stack == null) {
            contextStack = Collections.emptyList();
        } else {
            contextStack = stack.asList();
        }
        final boolean contextStackContainsTags = !contextStack.isEmpty();
        final Marker marker = event.getMarker();
        final boolean isEcsFieldsMarker = marker != null && EcsFieldsMarker.ECS_FIELDS_MARKER_NAME.equals(marker.getName());
        final boolean markerContainsTags = marker != null && (!isEcsFieldsMarker || marker.hasParents());
        if (contextStackContainsTags || markerContainsTags) {
            EcsJsonSerializer.serializeTagStart(builder);
            if (contextStackContainsTags) {
                final int len = contextStack.size();
                for (int i = 0; i < len; i++) {
                    builder.append('\"');
                    JsonUtils.quoteAsString(contextStack.get(i), builder);
                    builder.append("\",");
                }
            }
            if (markerContainsTags) {
                serializeMarkerTag(builder, marker);
            }
            EcsJsonSerializer.serializeTagEnd(builder);
        }
    }

    private void serializeMarkerTag(final StringBuilder builder, final Marker marker) {
        if (marker != null && !EcsFieldsMarker.ECS_FIELDS_MARKER_NAME.equals(marker.getName())) {
            EcsJsonSerializer.serializeSingleTag(builder, marker.getName());
        }
        if (marker.hasParents()) {
            final Marker[] parents = marker.getParents();
            for (int i = 0; i < parents.length; i++) {
                serializeMarkerTag(builder, parents[i]);
            }
        }
    }

    private void serializeCustomMarkers(final StringBuilder builder, final Marker marker) {
        if (marker != null) {
            EcsSerializer.serializeCustomMarker(builder, marker.getName());
            if (marker.hasParents()) {
                final Marker[] parents = marker.getParents();
                for (int i = 0; i < parents.length; i++) {
                    serializeCustomMarkers(builder, parents[i]);
                }
            }
        }
    }

    public void setMetaFields(final Map<String, String> metaFields) {
        additionalFields.addAll(metaFields.entrySet().stream().map(e -> new AdditionalField(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));
    }

    public static class Builder implements org.apache.logging.log4j.core.util.Builder<Log4j2JSONLayout> {

        @PluginConfiguration
        private Configuration configuration;

        @PluginBuilderAttribute("serviceName")
        private String serviceName;

        @PluginElement("AdditionalField")
        private KeyValuePair[] additionalFields = new KeyValuePair[] {};

        @PluginBuilderAttribute("locationInfo")
        private boolean locationInfo = false;

        @PluginBuilderAttribute("hostInfo")
        private boolean hostInfo = true;

        @PluginBuilderAttribute("addEventUuid")
        private boolean addEventUuid = true;

        Builder() {
        }

        public Configuration getConfiguration() {
            return configuration;
        }

        public Log4j2JSONLayout.Builder setConfiguration(final Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public KeyValuePair[] getAdditionalFields() {
            return additionalFields.clone();
        }

        public String getServiceName() {
            return serviceName;
        }

        public boolean isLocationInfo() {
            return locationInfo;
        }

        public boolean isHostInfo() {
            return hostInfo;
        }

        public boolean isAddEventUuid() {
            return addEventUuid;
        }

        /**
         * Additional fields to set on each log event.
         *
         * @return this builder
         */
        public Log4j2JSONLayout.Builder setAdditionalFields(final KeyValuePair[] additionalFields) {
            this.additionalFields = additionalFields.clone();
            return this;
        }

        public Log4j2JSONLayout.Builder setServiceName(final String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder setLocationInfo(final boolean locationInfo) {
            this.locationInfo = locationInfo;
            return this;
        }

        public Builder setHostInfo(final boolean hostInfo) {
            this.hostInfo = hostInfo;
            return this;
        }

        public Builder setAddEventUuid(final boolean addEventUuid) {
            this.addEventUuid = addEventUuid;
            return this;
        }

        @Override
        public Log4j2JSONLayout build() {
            return new Log4j2JSONLayout(getConfiguration(), serviceName, locationInfo, hostInfo, addEventUuid, additionalFields);
        }
    }
}
