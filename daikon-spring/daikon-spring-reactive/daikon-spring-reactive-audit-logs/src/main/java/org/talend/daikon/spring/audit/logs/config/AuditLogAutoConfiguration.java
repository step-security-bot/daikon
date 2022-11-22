package org.talend.daikon.spring.audit.logs.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.talend.daikon.spring.audit.common.config.AuditKafkaProperties;
import org.talend.daikon.spring.audit.common.config.AuditProperties;
import org.talend.daikon.spring.audit.logs.service.AuditLogCustomWebFilter;
import org.talend.daikon.spring.audit.logs.service.AuditLogSender;
import org.talend.daikon.spring.audit.logs.service.AuditLogSenderImpl;
import org.talend.daikon.spring.audit.service.AppAuditLogger;
import org.talend.logging.audit.AuditLoggerFactory;
import org.talend.logging.audit.LogAppenders;
import org.talend.logging.audit.impl.*;

import java.util.Properties;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties({ AuditProperties.class, AuditKafkaProperties.class })
@ConditionalOnProperty(value = "audit.enabled", havingValue = "true", matchIfMissing = true)
public class AuditLogAutoConfiguration {

    @Bean
    public AuditLogCustomWebFilter auditLogCustomWebFilter(final AuditLogSender auditLogSender) {
        return new AuditLogCustomWebFilter(auditLogSender);
    }

    @Bean
    public Properties kafkaProperties(AuditKafkaProperties auditKafkaProperties,
            @Value("${spring.application.name}") String applicationName) {
        Properties properties = new Properties();
        properties.put("application.name", applicationName);
        properties.put("backend", Backends.KAFKA.name());
        properties.put("log.appender", LogAppenders.NONE.name());
        properties.put("kafka.bootstrap.servers", auditKafkaProperties.getBootstrapServers());
        properties.put("kafka.topic", auditKafkaProperties.getTopic());
        properties.put("kafka.partition.key.name", auditKafkaProperties.getPartitionKeyName());
        properties.put("kafka.block.timeout.ms", auditKafkaProperties.getBlockTimeoutMs());
        return properties;
    }

    @Bean
    public AuditLogSender auditLogSender(AuditLoggerBase auditLoggerBase, Counter auditLogsGeneratedCounter) {
        AppAuditLogger auditLogger = AuditLoggerFactory.getEventAuditLogger(AppAuditLogger.class, auditLoggerBase);
        return new AuditLogSenderImpl(auditLogger, auditLogsGeneratedCounter);
    }

    @Bean
    public AuditLoggerBase auditLoggerBase(Properties kafkaProperties) {
        AuditConfigurationMap config = AuditConfiguration.loadFromProperties(kafkaProperties);
        return new SimpleAuditLoggerBase(config);
    }

    @Bean
    public Counter auditLogsGeneratedCounter(final MeterRegistry meterRegistry) {
        return Counter.builder("audit_logs_generated_count").description("The number of audit logs generated")
                .tag("source", "daikon-spring-audit-logs").register(meterRegistry);
    }
}
