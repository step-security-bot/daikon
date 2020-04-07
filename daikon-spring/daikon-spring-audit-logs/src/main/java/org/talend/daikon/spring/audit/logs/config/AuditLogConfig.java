package org.talend.daikon.spring.audit.logs.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.talend.daikon.spring.audit.logs.api.AuditUserProvider;
import org.talend.daikon.spring.audit.logs.service.GenerateAuditLogAspect;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties(AuditKafkaProperties.class)
@ConditionalOnProperty(value = "audit.enabled", havingValue = "true", matchIfMissing = true)
public class AuditLogConfig {

    @Bean
    public GenerateAuditLogAspect auditLogAspect(ObjectMapper objectMapper, AuditUserProvider auditUserProvider,
            AuditKafkaProperties auditKafkaProperties, @Value("${spring.application.name}") String applicationName) {
        return new GenerateAuditLogAspect(objectMapper, auditUserProvider, auditKafkaProperties, applicationName);
    }
}
