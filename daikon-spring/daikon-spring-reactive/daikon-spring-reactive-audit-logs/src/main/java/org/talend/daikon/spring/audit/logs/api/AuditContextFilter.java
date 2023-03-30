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
package org.talend.daikon.spring.audit.logs.api;

import org.talend.daikon.spring.audit.logs.service.AuditLogContextBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
