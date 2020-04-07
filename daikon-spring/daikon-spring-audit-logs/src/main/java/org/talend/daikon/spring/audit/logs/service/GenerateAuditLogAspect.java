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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.talend.daikon.spring.audit.logs.api.AuditContextFilter;
import org.talend.daikon.spring.audit.logs.api.AuditUserProvider;
import org.talend.daikon.spring.audit.logs.api.GenerateAuditLog;
import org.talend.daikon.spring.audit.logs.config.AuditKafkaProperties;
import org.talend.logging.audit.AuditLoggerFactory;
import org.talend.logging.audit.Context;
import org.talend.logging.audit.LogAppenders;
import org.talend.logging.audit.impl.AuditConfiguration;
import org.talend.logging.audit.impl.AuditConfigurationMap;
import org.talend.logging.audit.impl.Backends;
import org.talend.logging.audit.impl.SimpleAuditLoggerBase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Aspect
public class GenerateAuditLogAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateAuditLogAspect.class);

    private final ObjectMapper objectMapper;

    private final AuditUserProvider auditUserProvider;

    private final AuditLogger auditLogger;

    private final AuditKafkaProperties auditKafkaProperties;

    public GenerateAuditLogAspect(ObjectMapper objectMapper, AuditUserProvider auditUserProvider,
            AuditKafkaProperties auditKafkaProperties, String applicationName) {
        this.objectMapper = objectMapper;
        this.auditUserProvider = auditUserProvider;
        this.auditKafkaProperties = auditKafkaProperties;
        Properties properties = getProperties(auditKafkaProperties, applicationName);
        AuditConfigurationMap config = AuditConfiguration.loadFromProperties(properties);
        this.auditLogger = AuditLoggerFactory.getEventAuditLogger(AuditLogger.class, new SimpleAuditLoggerBase(config));
    }

    private Properties getProperties(AuditKafkaProperties auditKafkaProperties, String applicationName) {
        Properties properties = new Properties();
        properties.put("application.name", applicationName);
        properties.put("backend", Backends.KAFKA.name());
        properties.put("log.appender", LogAppenders.NONE.name());
        properties.put("kafka.bootstrap.servers", auditKafkaProperties.getBootstrapServers());
        properties.put("kafka.topic", auditKafkaProperties.getTopic());
        properties.put("kafka.partition.key.name", auditKafkaProperties.getPartitionKeyName());
        return properties;
    }

    @Around("@annotation(org.talend.daikon.spring.audit.logs.api.GenerateAuditLog)")
    public Object auditLogGeneration(final ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Method method = signature.getMethod();
        GenerateAuditLog auditLogAnnotation = method.getAnnotation(GenerateAuditLog.class);

        // Retrieve HTTP request & response
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();

        Annotation[][] parameterAnnotations = ((MethodSignature) proceedingJoinPoint.getSignature()).getMethod()
                .getParameterAnnotations();
        AtomicReference<Integer> argumentIndex = new AtomicReference<>();
        AtomicInteger index = new AtomicInteger();
        Arrays.asList(parameterAnnotations).forEach(annotations -> {
            if (Arrays.stream(annotations)
                    .anyMatch(annotation -> annotation.annotationType().getName().equals(RequestBody.class.getName()))) {
                argumentIndex.set(index.intValue());
            }
            index.getAndIncrement();
        });
        Object requestBody = null;
        if (argumentIndex.get() != null) {
            requestBody = proceedingJoinPoint.getArgs()[argumentIndex.get()];
        }

        // Run original method
        Object responseObject;
        try {
            responseObject = proceedingJoinPoint.proceed();
            sendAuditLog(request, requestBody, response != null ? response.getStatus() : 0, responseObject, auditLogAnnotation);
            return responseObject;
        } catch (Throwable throwable) {
            sendAuditLog(request, requestBody, HttpStatus.INTERNAL_SERVER_ERROR.value(), null, auditLogAnnotation);
            throw throwable;
        }

    }

    private void sendAuditLog(HttpServletRequest request, Object requestBody, int responseCode, Object responseObject,
            GenerateAuditLog auditLogAnnotation) throws JsonProcessingException {
        AuditLogContextBuilder auditLogContextBuilder = AuditLogContextBuilder.create()
                .withTimestamp(OffsetDateTime.now().toString()).withLogId(UUID.randomUUID()).withRequestId(UUID.randomUUID())
                .withApplicationId(auditLogAnnotation.application()).withEventType(auditLogAnnotation.eventType())
                .withEventCategory(auditLogAnnotation.eventCategory()).withEventOperation(auditLogAnnotation.eventOperation())
                .withUserId(auditUserProvider.getUserId()).withUsername(auditUserProvider.getUsername())
                .withEmail(auditUserProvider.getUserEmail()).withAccountId(auditUserProvider.getAccountId())
                .withRequest(request, requestBody).withResponse(responseCode,
                        (auditLogAnnotation.includeBodyResponse() && responseObject != null)
                                ? objectMapper.writeValueAsString(responseObject)
                                : null);

        try {
            AuditContextFilter filter = auditLogAnnotation.filter().getDeclaredConstructor().newInstance();
            auditLogContextBuilder = filter.filter(auditLogContextBuilder, requestBody);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        auditLogger.sendAuditLog(auditLogContextBuilder.build());

        LOGGER.info("audit log generated with metadata {}", auditLogAnnotation);
    }
}