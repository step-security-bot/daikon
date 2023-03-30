package org.talend.daikon.spring.metrics.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;

@AutoConfiguration
@EnableAspectJAutoProxy
@ComponentScan(basePackageClasses = MetricsConfiguration.class)
public class MetricsConfiguration {

    @Bean
    @ConditionalOnBean({ Tracer.class, MeterRegistry.class })
    public Aspects metricAspect(@Autowired Tracer tracer, @Autowired MeterRegistry repository) {
        return new Aspects(tracer, repository);
    }
}
