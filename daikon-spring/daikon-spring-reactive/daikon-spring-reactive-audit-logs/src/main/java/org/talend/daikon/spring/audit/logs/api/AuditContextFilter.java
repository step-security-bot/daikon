package org.talend.daikon.spring.audit.logs.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.talend.daikon.spring.audit.logs.service.AuditLogContextBuilder;

/**
 * Audit log context filter interface
 */
public interface AuditContextFilter {

    /**
     * Filter the audit log context before sending it
     *
     * @param auditLogContextBuilder audit log context builder
     * @param requestBody HTTP request body to filter
     * @param responseBody HTTP request body to filter
     * @return the filtered audit log context
     */
    AuditLogContextBuilder filter(AuditLogContextBuilder auditLogContextBuilder, String requestBody, String responseBody);

    default <T> T parse(ObjectMapper objectMapper, String str, Class<T> type) {
        if (str != null && type != null) {
            try {
                return objectMapper.readValue(str, type);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    default String toJson(ObjectMapper objectMapper, Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
