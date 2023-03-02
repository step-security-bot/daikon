// ============================================================================
//
// Copyright (C) 2006-2023 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.daikon.spring.audit.logs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.talend.daikon.exception.ExceptionContext;
import org.talend.daikon.exception.error.CommonErrorCodes;
import org.talend.daikon.spring.audit.common.exception.AuditLogException;
import org.talend.daikon.spring.audit.common.model.AuditLogFieldEnum;
import org.talend.logging.audit.Context;
import org.talend.logging.audit.impl.DefaultContextImpl;

import java.util.*;

import static org.talend.daikon.spring.audit.common.model.AuditLogFieldEnum.*;

@Data
@Builder
@With
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogContextBuilder {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    private final Map<String, String> context = new LinkedHashMap<>();

    private final Map<String, Object> request = new LinkedHashMap<>();

    private final Map<String, Object> response = new LinkedHashMap<>();

    private ServerHttpRequest serverHttpRequest;

    public Context build() throws AuditLogException {
        try {
            context.values().removeAll(Collections.singletonList(null));
            request.values().removeAll(Collections.singletonList(null));
            response.values().removeAll(Collections.singletonList(null));

            if (!request.isEmpty()) {
                request.replaceAll((k, v) -> convertToString(v));
                context.put(REQUEST.getId(), objectMapper.writeValueAsString(request));
            }
            if (!response.isEmpty()) {
                response.replaceAll((k, v) -> convertToString(v));
                context.put(RESPONSE.getId(), objectMapper.writeValueAsString(response));
            }
            checkAuditContextIsValid();
            return new DefaultContextImpl(context);
        } catch (JsonProcessingException e) {
            throw new AuditLogException(CommonErrorCodes.UNABLE_TO_SERIALIZE_TO_JSON, e);
        }
    }

    public void checkAuditContextIsValid() throws AuditLogException {
        // check elements of the context
        List<AuditLogFieldEnum> notFound = new ArrayList<>();
        for (AuditLogFieldEnum auditLogFieldEnum : AuditLogFieldEnum.values()) {
            if (auditLogFieldEnum.isMandatory() && !auditLogFieldEnum.hasParent()) {
                if (!context.containsKey(auditLogFieldEnum.getId())
                        || !StringUtils.hasText(context.get(auditLogFieldEnum.getId()))) {
                    notFound.add(auditLogFieldEnum);
                }
            }
        }
        // then check request and response
        for (AuditLogFieldEnum requestChild : REQUEST.getChildren()) {
            if (requestChild.isMandatory() && (!request.containsKey(requestChild.getId())
                    || !StringUtils.hasText(String.valueOf(request.get(requestChild.getId()))))) {
                notFound.add(requestChild);
            }
        }
        for (AuditLogFieldEnum responseChild : RESPONSE.getChildren()) {
            if (responseChild.isMandatory() && (!response.containsKey(responseChild.getId())
                    || !StringUtils.hasText(String.valueOf(response.get(responseChild.getId()))))) {
                notFound.add(responseChild);
            }
        }

        if (!notFound.isEmpty()) {
            throw new AuditLogException(CommonErrorCodes.UNEXPECTED_EXCEPTION, ExceptionContext.withBuilder()
                    .put(ExceptionContext.KEY_MESSAGE, "audit log context is incomplete, missing information: " + notFound)
                    .build());
        }
    }

    private String convertToString(Object value) {
        String stringValue = null;
        if (value instanceof String) {
            stringValue = (String) value;
        } else if (value != null) {
            try {
                stringValue = objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new AuditLogException(CommonErrorCodes.UNABLE_TO_SERIALIZE_TO_JSON, e);
            }
        }
        return stringValue;
    }

    public void addContextInfo(final String key, final String value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        this.context.put(key, value);
    }

    public void addRequestInfo(final String key, final Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        this.request.put(key, value);
    }

    public void addResponseInfo(final String key, final Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        this.response.put(key, value);
    }

    public void setRequestBody(String requestBody) {
        addRequestInfo(REQUEST_BODY.getId(), requestBody);
    }

    public void setResponseBody(String responseBody) {
        addResponseInfo(RESPONSE_BODY.getId(), responseBody);
    }
}
