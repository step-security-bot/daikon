package org.talend.daikon.spring.audit.logs.api;

import org.talend.daikon.spring.audit.logs.service.AuditLogContextBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class TestFilter implements AuditContextFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public AuditLogContextBuilder filter(AuditLogContextBuilder auditLogContextBuilder, String requestBody, String responseBody) {

        TestBody request = parse(objectMapper, requestBody, TestBody.class);
        TestBody response = parse(objectMapper, responseBody, TestBody.class);

        if (request != null) {
            auditLogContextBuilder.setRequestBody(toJson(objectMapper, request.withPassword(null)));
        }

        if (response != null) {
            auditLogContextBuilder.setResponseBody(toJson(objectMapper, response.withPassword(null)));
        }

        return auditLogContextBuilder;
    }
}
