package org.talend.daikon.spring.audit.logs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.talend.logging.audit.Context;
import org.talend.logging.audit.impl.DefaultContextImpl;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class AuditLogContextBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogContextBuilder.class);

    private final Map<String, String> context = new LinkedHashMap<>();

    private final Map<String, String> request = new LinkedHashMap<>();

    private final Map<String, String> response = new LinkedHashMap<>();

    private ObjectMapper objectMapper = new ObjectMapper();

    private AuditLogContextBuilder() {
    }

    public static Context emptyContext() {
        return new DefaultContextImpl();
    }

    public static AuditLogContextBuilder create() {
        return new AuditLogContextBuilder();
    }

    private AuditLogContextBuilder with(String key, String value) {
        return with(key, value, context);
    }

    private AuditLogContextBuilder with(String key, String value, Map<String, String> contextMap) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        contextMap.put(key, value);
        return this;
    }

    public AuditLogContextBuilder withTimestamp(String timestamp) {
        return this.with("timestamp", timestamp);
    }

    public AuditLogContextBuilder withLogId(UUID logId) {
        return this.with("log_id", logId.toString());
    }

    public AuditLogContextBuilder withRequestId(UUID requestId) {
        return this.with("request_id", requestId.toString());
    }

    public AuditLogContextBuilder withAccountId(String accountId) {
        return this.with("account_id", accountId);
    }

    public AuditLogContextBuilder withUserId(String userId) {
        return this.with("user_id", userId);
    }

    public AuditLogContextBuilder withUsername(String username) {
        return this.with("username", username);
    }

    public AuditLogContextBuilder withEmail(String email) {
        return this.with("email", email);
    }

    public AuditLogContextBuilder withApplicationId(String applicationId) {
        return this.with("application_id", applicationId);
    }

    public AuditLogContextBuilder withEventType(String eventType) {
        return this.with("event_type", eventType);
    }

    public AuditLogContextBuilder withEventCategory(String eventCategory) {
        return this.with("event_category", eventCategory);
    }

    public AuditLogContextBuilder withEventOperation(String eventOperation) {
        return this.with("event_operation", eventOperation);
    }

    public AuditLogContextBuilder withClientIp(String clientIp) {
        return this.with("client_ip", clientIp);
    }

    public AuditLogContextBuilder withRequestUrl(String requestUrl) {
        return this.with("url", requestUrl, request);
    }

    public AuditLogContextBuilder withRequestMethod(String requestMethod) {
        return this.with("method", requestMethod, request);
    }

    public AuditLogContextBuilder withRequestUserAgent(String requestUserAgent) {
        return this.with("user_agent", requestUserAgent, request);
    }

    public AuditLogContextBuilder withRequestBody(String requestBody) {
        return this.with("body", requestBody, request);
    }

    public AuditLogContextBuilder withResponseCode(int httpStatus) {
        return this.with("code", String.valueOf(httpStatus), response);
    }

    public AuditLogContextBuilder withResponseBody(Object body) {
        return this.with("body", getJsonValue(body), response);
    }

    private String getJsonValue(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            LOGGER.warn("audit log response body could not be built, from {}", body);
            return "";
        }
    }

    public Context build() {
        try {
            if (!request.isEmpty()) {
                context.put("request", objectMapper.writeValueAsString(request));
            }
            if (!response.isEmpty()) {
                context.put("response", objectMapper.writeValueAsString(response));
            }
            return new DefaultContextImpl(context);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public AuditLogContextBuilder withRequest(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return withClientIp(request.getRemoteAddr()).withRequestUrl(request.getRequestURL().toString())
                .withRequestMethod(request.getMethod()).withRequestUserAgent(userAgent);
    }

    public AuditLogContextBuilder withResponse(int httpStatus, Object body) {
        AuditLogContextBuilder builder = withResponseCode(httpStatus);
        if (body != null) {
            return builder.withResponseBody(body);
        }
        return builder;
    }
}
