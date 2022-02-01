package org.talend.daikon.logging.ecs;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.talend.daikon.logging.config.LoggingProperties;
import org.talend.daikon.logging.event.field.HostData;

import co.elastic.logging.AdditionalField;
import co.elastic.logging.EcsJsonSerializer;
import co.elastic.logging.JsonUtils;

/**
 * Utility ECS serializer class
 */
public class EcsSerializer {

    private static final String ECS_VERSION = LoggingProperties.get("ecs.version");

    private static final String LEGACY_MDC_PREFIX = "customInfo.";

    /**
     * Serialize the additional fields (mapped and filtered)
     *
     * @param builder the builder to serialize in
     * @param additionalFields the additional fields to serialize
     */
    public static void serializeAdditionalFields(final StringBuilder builder, final List<AdditionalField> additionalFields) {
        EcsJsonSerializer.serializeAdditionalFields(builder, additionalFields.stream()
                // Map additional field keys with corresponding ECS field
                .map(f -> new AdditionalField(MdcEcsMapper.map(f.getKey()), f.getValue()))
                // Filter out non ECS fields if in strict mode
                .filter(f -> EcsFieldsChecker.isECSField(f.getKey())).collect(Collectors.toList()));
    }

    /**
     * Serialize the MDC (mapped and filtered) with numeric fields formatted as numbers
     * Field type is defined by https://github.com/elastic/ecs/blob/master/generated/ecs/ecs_flat.yml
     *
     * @param builder the builder to serialize in
     * @param mdcPropertyMap the MDC to serialize
     */
    public static void serializeMDC(final StringBuilder builder, final Map<String, String> mdcPropertyMap) {
        serializeMDC(builder, mdcPropertyMap, Collections.emptyMap(), false);
    }

    /**
     * Serialize the MDC (mapped and filtered)
     *
     * @param builder the builder to serialize in
     * @param mdcPropertyMap the MDC to serialize
     * @param legacyMode if true it will allow non-ECS fields + it will add a prefix to non-standard MDC values
     */
    public static void serializeMDC(final StringBuilder builder, final Map<String, String> mdcPropertyMap,
            final Map<String, String> metaFields, final boolean legacyMode) {
        if (null == builder || null == mdcPropertyMap || mdcPropertyMap.isEmpty()) {
            return;
        }

        final Map<String, String> filteredMdc = mdcPropertyMap.entrySet().stream()
                // Map additional field keys with corresponding ECS field
                .map(mdcEntry -> new AbstractMap.SimpleEntry<>(MdcEcsMapper.map(mdcEntry.getKey()), mdcEntry.getValue()))
                // Filter out non ECS fields
                .filter(mdcEntry -> legacyMode || EcsFieldsChecker.isECSField(mdcEntry.getKey()))
                .map(mdcEntry -> new AbstractMap.SimpleEntry<>(
                        metaFields.getOrDefault(mdcEntry.getKey(), (legacyMode ? LEGACY_MDC_PREFIX : "") + mdcEntry.getKey()),
                        mdcEntry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final List<String> mdcNumericFields = filteredMdc.keySet().stream().filter(EcsFields::isNumber)
                .collect(Collectors.toList());

        // serialize numeric values
        if (!mdcNumericFields.isEmpty() && !filteredMdc.isEmpty()) {
            mdcNumericFields.stream().map(MdcEcsMapper::map)
                    .map(mappedFieldName -> new AbstractMap.SimpleEntry<>(mappedFieldName, filteredMdc.get(mappedFieldName)))
                    .peek(entry -> filteredMdc.remove(entry.getKey()))
                    .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                    .filter(entry -> NumberUtils.isParsable(entry.getValue()))
                    .forEach(e -> builder.append("\"").append(e.getKey()).append("\":").append(e.getValue()).append(","));
        }

        // serialize string values
        EcsJsonSerializer.serializeMDC(builder, filteredMdc);
    }

    /**
     * Serialize the host data
     *
     * @param builder the builder to serialize in
     * @param hostData the host data to serialize
     */
    public static void serializeHostInfo(final StringBuilder builder, final HostData hostData) {
        builder.append(String.format("\"%s\":[\"%s\"],", EcsFields.HOST_IP.fieldName, hostData.getHostAddress()));
        builder.append(String.format("\"%s\":\"%s\",", EcsFields.HOST_HOSTNAME.fieldName, hostData.getHostName()));
    }

    /**
     * Serialize the event id
     *
     * @param builder the builder to serialize in
     * @param eventId the event id to serialize
     */
    public static void serializeEventId(final StringBuilder builder, final UUID eventId) {
        builder.append(String.format("\"%s\":\"%s\",", EcsFields.EVENT_ID.fieldName, eventId));
    }

    /**
     * serialize the custom markers (following key:value pattern)
     *
     * @param builder the builder to serialize in
     * @param marker the marker to serialize
     */
    public static void serializeCustomMarker(final StringBuilder builder, final String marker) {
        if (marker != null) {
            final String[] customMarker = marker.split(":");
            if (customMarker.length == 2 && customMarker[0] != null) {
                final String markerKey = EcsFieldsChecker.isECSField(MdcEcsMapper.map(customMarker[0]))
                        ? MdcEcsMapper.map(customMarker[0])
                        : "labels." + customMarker[0];
                builder.append(String.format("\"%s\":\"%s\",", markerKey, customMarker[1]));
            }
        }
    }

    /**
     * Serialize the ECS version based on the value defined as property
     *
     * @param builder the builder to serialize in
     */
    public static void serializeEcsVersion(final StringBuilder builder) {
        builder.append("\"ecs.version\":\"");
        builder.append(ECS_VERSION);
        builder.append("\",");
    }

    /**
     * Serialize ECS Categorization Fields.
     * See https://www.elastic.co/guide/en/ecs/1.8/ecs-category-field-values-reference.html
     *
     * @param builder
     */
    public static void serializeHttpEventCategorizationFields(final StringBuilder builder) {
        builder.append("\"").append(EcsFields.EVENT_KIND.fieldName).append("\":\"event\", ").append("\"")
                .append(EcsFields.EVENT_CATEGORY.fieldName).append("\":\"web\", ").append("\"")
                .append(EcsFields.EVENT_TYPE.fieldName).append("\":\"access\", ");
    }

    /**
     * Serialize the http status code.
     *
     * @param builder the builder to serialize in
     * @param statusCode
     */
    public static void serializeHttpStatusCode(final StringBuilder builder, final int statusCode) {
        builder.append("\"").append(EcsFields.HTTP_RESPONSE_STATUS_CODE.fieldName).append("\":").append(statusCode).append(",");
    }

    public static void serializeEventOutcome(final StringBuilder builder, final int statusCode) {
        String outcome = "success";
        if (statusCode >= 400) {
            outcome = "failure";
        }
        builder.append("\"").append(EcsFields.EVENT_OUTCOME.fieldName).append("\":\"").append(outcome).append("\",");
    }

    public static void serializeHttpMethod(final StringBuilder builder, final String method) {
        builder.append("\"").append(EcsFields.HTTP_REQUEST_METHOD.fieldName).append("\":\"").append(method).append("\",");
    }

    /**
     * Serialize the User agent request header.
     *
     * @param builder the builder to serialize in
     * @param userAgent optional user agent header
     */
    public static void serializeUserAgent(final StringBuilder builder, final String userAgent) {
        if (userAgent != null) {
            builder.append("\"").append(EcsFields.USER_AGENT_ORIGINAL.fieldName).append("\":\"").append(userAgent).append("\", ");
        }
    }

    public static void serializeClientIp(final StringBuilder builder, final String clientIp) {
        if (clientIp != null) {
            builder.append("\"").append(EcsFields.CLIENT_IP.fieldName).append("\":\"").append(clientIp).append("\",");
        }
    }

    public static void serializeClientPort(final StringBuilder builder, final int clientPort) {
        if (clientPort > 0) {
            builder.append("\"").append(EcsFields.CLIENT_PORT.fieldName).append("\":").append(clientPort).append(",");
        }
    }

    public static void serializeNetworkProtocol(final StringBuilder builder, final String protocol) {
        builder.append("\"").append(EcsFields.NETWORK_PROTOCOL.fieldName).append("\":\"").append(protocol).append("\",");
    }

    public static void serializeUrlScheme(final StringBuilder builder, final String scheme) {
        builder.append("\"").append(EcsFields.URL_SCHEME.fieldName).append("\":\"").append(scheme).append("\",");
    }

    public static void serializeHttpVersion(final StringBuilder builder, final String version) {
        if (version != null) {
            builder.append("\"").append(EcsFields.HTTP_VERSION.fieldName).append("\":\"").append(version).append("\",");
        }
    }

    public static void serializeHttpRequestBodyBytes(final StringBuilder builder, final long requestBodyLength) {
        builder.append("\"").append(EcsFields.HTTP_REQUEST_BODY_BYTES.fieldName).append("\":").append(requestBodyLength)
                .append(",");
    }

    public static void serializeHttpResponseBodyBytes(final StringBuilder builder, final long contentLength) {
        builder.append("\"").append(EcsFields.HTTP_RESPONSE_BODY_BYTES.fieldName).append("\":").append(contentLength).append(",");
    }

    /**
     * Serialize the event duration in nanoseconds.
     *
     * @param builder
     * @param duration
     */
    public static void serializeEventDuration(final StringBuilder builder, final long duration) {
        builder.append("\"").append(EcsFields.EVENT_DURATION.fieldName).append("\":").append(duration).append(",");
    }

    /**
     * Serialize the event start date.
     *
     * @param builder
     * @param eventStart
     */
    public static void serializeEventStart(final StringBuilder builder, final long eventStart) {
        if (eventStart > 0) {
            builder.append("\"").append(EcsFields.EVENT_START.fieldName).append("\":\"").append(Instant.ofEpochMilli(eventStart))
                    .append("\",");
        }
    }

    /**
     * Serialize the event end date.
     *
     * @param builder
     * @param eventEnd
     */
    public static void serializeEventEnd(final StringBuilder builder, final long eventEnd) {
        if (eventEnd > 0) {
            builder.append("\"").append(EcsFields.EVENT_END.fieldName).append("\":\"").append(Instant.ofEpochMilli(eventEnd))
                    .append("\",");
        }
    }

    /**
     * Serialize the request query string if any.
     *
     * @param builder
     * @param queryString
     */
    public static void serializeUrlQuery(final StringBuilder builder, final String queryString) {
        if (queryString != null) {
            builder.append("\"").append(EcsFields.URL_QUERY.fieldName).append("\":\"").append(queryString).append("\",");
        }
    }

    /**
     * Serialize the request uri path.
     *
     * @param builder
     * @param path
     */
    public static void serializeUrlPath(final StringBuilder builder, final String path) {
        builder.append("\"").append(EcsFields.URL_PATH.fieldName).append("\":\"").append(path).append("\",");
    }

    /**
     * Serialize the request username if any.
     *
     * @param builder
     * @param username
     */
    public static void serializeUrlUser(final StringBuilder builder, final String username) {
        if (username != null) {
            builder.append("\"").append(EcsFields.URL_USERNAME.fieldName).append("\":\"").append(username).append("\",");
        }
    }

    public static void serializeTraceId(final StringBuilder builder, final String traceId) {
        if (traceId != null) {
            builder.append("\"").append(EcsFields.TRACE_ID.fieldName).append("\":\"").append(traceId).append("\",");
        }
    }

    public static void serializeSpanId(final StringBuilder builder, final String spanId) {
        if (spanId != null) {
            builder.append("\"").append(EcsFields.SPAN_ID.fieldName).append("\":\"").append(spanId).append("\",");
        }
    }

    public static void serialiseHttpRequestHeaders(final StringBuilder builder, final Map<String, String> requestHeaderMap) {
        builder.append("\"http.request.headers\":{");
        EcsJsonSerializer.serializeMDC(builder, requestHeaderMap.entrySet().stream().filter(
                e -> !ArrayUtils.contains(new String[] { "authorization", "host", "user-agent" }, e.getKey().toLowerCase()))
                .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue)));
        EcsJsonSerializer.removeIfEndsWith(builder, ",");
        builder.append("}, ");
    }

    public static void serializeHttpResponseHeaders(final StringBuilder builder, final Map<String, String> responseHeaderMap) {
        builder.append("\"http.response.headers\":{");
        EcsJsonSerializer.serializeMDC(builder,
                responseHeaderMap.entrySet().stream()
                        .filter(e -> !ArrayUtils.contains(new String[] { "set-cookie", "date" }, e.getKey().toLowerCase()))
                        .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue)));
        EcsJsonSerializer.removeIfEndsWith(builder, ",");
        builder.append("}, ");
    }

    public static void serializeEcsFieldsMarker(final StringBuilder builder, final EcsFieldsMarker ecsFieldsMarker) {
        if (ecsFieldsMarker != null) {
            ecsFieldsMarker.getEcsFieldSets().values().forEach(ecsFieldSet -> {
                ecsFieldSet.getItemsToSerialize().forEach((ecsFieldType, item) -> {
                    serializeEcsFieldItem(builder, ecsFieldType, item);
                });
            });
        }
    }

    static void serializeEcsFieldItem(final StringBuilder builder, final EcsFields ecsFieldType, final Object item) {
        if (item instanceof Float) {
            addToSerializationWithoutQuote(builder, ecsFieldType.fieldName, item);
        } else if (item instanceof Long) {
            addToSerializationWithoutQuote(builder, ecsFieldType.fieldName, item);
        } else if (item instanceof Integer) {
            addToSerializationWithoutQuote(builder, ecsFieldType.fieldName, item);
        } else if (item instanceof String) {
            addToSerializationWithQuotes(builder, ecsFieldType.fieldName, (String) item);
        } else if (item instanceof Date) {
            final Date date = (Date) item;
            addToSerializationWithQuotes(builder, ecsFieldType.fieldName, date.toInstant().toString());
        } else if (item instanceof Map && EcsFields.LABELS == ecsFieldType) {
            ((Map<String, String>) item).forEach((key, value) -> addToSerializationWithQuotes(builder, key, value));
        }
    }

    private static void addToSerializationWithQuotes(final StringBuilder builder, final String key, final String value) {
        if (key != null) {
            builder.append('\"');
            JsonUtils.quoteAsString(key, builder);
            builder.append("\":\"");
            JsonUtils.quoteAsString(value, builder);
            builder.append("\",");
        }
    }

    private static void addToSerializationWithoutQuote(final StringBuilder builder, final String key, final Object value) {
        if (key != null) {
            builder.append('\"');
            JsonUtils.quoteAsString(key, builder);
            builder.append("\":").append(value).append(",");
        }
    }

}
