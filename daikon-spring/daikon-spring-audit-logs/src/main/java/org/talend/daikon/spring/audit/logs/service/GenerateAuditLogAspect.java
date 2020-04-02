package org.talend.daikon.spring.audit.logs.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.talend.daikon.spring.audit.logs.api.AuditUserProvider;
import org.talend.daikon.spring.audit.logs.api.GenerateAuditLog;
import org.talend.logging.audit.AuditLoggerFactory;
import org.talend.logging.audit.Context;
import org.talend.logging.audit.impl.SimpleAuditLoggerBase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.UUID;

@Aspect
@Component
public class GenerateAuditLogAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateAuditLogAspect.class);

    private final ObjectMapper objectMapper;

    private final AuditUserProvider auditUserProvider;

    private final AuditLogger auditLogger;

    public GenerateAuditLogAspect(ObjectMapper objectMapper, AuditUserProvider auditUserProvider) {
        this.objectMapper = objectMapper;
        this.auditUserProvider = auditUserProvider;
        this.auditLogger = AuditLoggerFactory.getEventAuditLogger(AuditLogger.class, new SimpleAuditLoggerBase());
    }

    @Around("@annotation(org.talend.audit.logs.api.logger.GenerateAuditLog)")
    public Object auditLogGeneration(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        GenerateAuditLog auditLogAnnotation = method.getAnnotation(GenerateAuditLog.class);

        // Retrieve HTTP request & response
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();

        // Run original method
        Object responseObject;
        try {
            responseObject = proceedingJoinPoint.proceed();
            sendAuditLog(request, response != null ? response.getStatus() : 0, responseObject, auditLogAnnotation);
            return responseObject;
        } catch (Throwable throwable) {
            sendAuditLog(request, HttpStatus.INTERNAL_SERVER_ERROR.value(), null, auditLogAnnotation);
            throw throwable;
        }

    }

    private void sendAuditLog(HttpServletRequest request, int responseCode, Object responseObject,
            GenerateAuditLog auditLogAnnotation) throws JsonProcessingException {
        Context context = AuditLogContextBuilder.create().withTimestamp(OffsetDateTime.now().toString())
                .withLogId(UUID.randomUUID()).withRequestId(UUID.randomUUID()).withApplicationId(auditLogAnnotation.application())
                .withEventType(auditLogAnnotation.eventType()).withEventCategory(auditLogAnnotation.eventCategory())
                .withEventOperation(auditLogAnnotation.eventOperation()).withUserId(auditUserProvider.getUserId())
                .withUsername(auditUserProvider.getUsername()).withEmail(auditUserProvider.getUserEmail())
                .withAccountId(auditUserProvider.getAccountId()).withRequest(request)
                .withResponse(responseCode,
                        (auditLogAnnotation.includeBodyResponse() && responseObject != null)
                                ? objectMapper.writeValueAsString(responseObject)
                                : null)
                .build();

        auditLogger.sendAuditLog(context);

        LOGGER.info("audit log generated with metadata {}", auditLogAnnotation);
    }

}