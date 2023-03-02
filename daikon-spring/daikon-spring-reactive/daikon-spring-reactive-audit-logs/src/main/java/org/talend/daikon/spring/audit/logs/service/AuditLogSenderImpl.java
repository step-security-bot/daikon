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

import io.micrometer.core.instrument.Counter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.talend.daikon.spring.audit.common.exception.AuditLogException;
import org.talend.daikon.spring.audit.logs.api.AuditContextFilter;
import org.talend.daikon.spring.audit.logs.api.GenerateAuditLog;
import org.talend.daikon.spring.audit.service.AppAuditLogger;
import org.talend.logging.audit.Context;

import java.lang.reflect.InvocationTargetException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.talend.daikon.spring.audit.common.model.AuditLogFieldEnum.*;

@Slf4j
@AllArgsConstructor
public class AuditLogSenderImpl implements AuditLogSender {

    private final AppAuditLogger auditLogger;

    private final Counter auditLogsGeneratedCounter;

    /**
     * Build the context and send the audit log
     */
    @Override
    public void sendAuditLog(String tenant, ServerHttpRequest request, String requestBody, int responseCode, String responseBody,
            GenerateAuditLog auditLogAnnotation) {
        try {
            // Build context from request, response & annotation info
            AuditLogContextBuilder auditLogContextBuilder = AuditLogContextBuilder.builder().serverHttpRequest(request).build();

            // Context
            auditLogContextBuilder.addContextInfo(TIMESTAMP.getId(), OffsetDateTime.now().toString());
            auditLogContextBuilder.addContextInfo(LOG_ID.getId(), UUID.randomUUID().toString());
            auditLogContextBuilder.addContextInfo(REQUEST_ID.getId(), UUID.randomUUID().toString());
            auditLogContextBuilder.addContextInfo(APPLICATION_ID.getId(), auditLogAnnotation.application());
            auditLogContextBuilder.addContextInfo(EVENT_TYPE.getId(), auditLogAnnotation.eventType());
            auditLogContextBuilder.addContextInfo(EVENT_CATEGORY.getId(), auditLogAnnotation.eventCategory());
            auditLogContextBuilder.addContextInfo(EVENT_OPERATION.getId(), auditLogAnnotation.eventOperation());
            auditLogContextBuilder.addContextInfo(ACCOUNT_ID.getId(), tenant);

            // Request
            auditLogContextBuilder.addContextInfo(CLIENT_IP.getId(),
                    request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress()
                            : request.getHeaders().getFirst(CLIENT_IP.name()));
            auditLogContextBuilder.addRequestInfo(METHOD.getId(), request.getMethod() != null ? request.getMethod().name() : "");
            auditLogContextBuilder.addRequestInfo(URL.getId(), request.getURI().toString());
            String userAgent = request.getHeaders().getFirst("User-Agent");
            auditLogContextBuilder.addContextInfo(USER_AGENT.getId(), userAgent);
            auditLogContextBuilder.addRequestInfo(REQUEST_BODY.getId(), requestBody);

            // Response
            auditLogContextBuilder.addResponseInfo(RESPONSE_CODE.getId(), responseCode);
            auditLogContextBuilder.addResponseInfo(RESPONSE_BODY.getId(),
                    auditLogAnnotation.includeBodyResponse() ? responseBody : "");

            // Filter the context if needed
            AuditContextFilter filter = auditLogAnnotation.filter().getDeclaredConstructor().newInstance();
            auditLogContextBuilder = filter.filter(auditLogContextBuilder, requestBody, responseBody);
            // Compute request fields only at build step to leverage ip extractor

            log.debug("Sending audit log: {}", auditLogContextBuilder);
            // Finally send the log
            this.sendAuditLog(auditLogContextBuilder.build());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
                | AuditLogException e) {
            log.error("audit log with metadata {} has not been generated", auditLogAnnotation, e);
        }
    }

    /**
     * Send the audit log
     */
    @Override
    public void sendAuditLog(Context context) {
        try {
            auditLogger.sendAuditLog(context);
        } catch (Exception e) {
            // Clean audit log context from PIIs
            List<String> toKeep = Arrays.asList(TIMESTAMP.getId(), APPLICATION_ID.getId(), ACCOUNT_ID.getId(), EVENT_TYPE.getId(),
                    EVENT_CATEGORY.getId(), EVENT_OPERATION.getId());
            context.keySet().retainAll(toKeep);
            log.warn("Error sending audit logs to Kafka : {}", context, e);
        } finally {
            auditLogsGeneratedCounter.increment();
        }
    }

}
