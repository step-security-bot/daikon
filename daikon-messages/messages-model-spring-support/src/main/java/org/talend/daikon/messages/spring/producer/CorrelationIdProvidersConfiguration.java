// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.daikon.messages.spring.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.talend.daikon.messages.header.producer.CorrelationIdProvider;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

@AutoConfiguration
@ConditionalOnClass({ Tracer.class })
@AutoConfigureBefore({ DefaultProducerProvidersConfiguration.class })
public class CorrelationIdProvidersConfiguration {

    @Value("${org.talend.daikon.messages.spring.producer.sleuth.defaultSpanName:MESSAGING_DEFAULT_SPAN")
    private String defaultSpanName;

    @Bean
    @ConditionalOnProperty(prefix = "management.tracing", name = "enabled", matchIfMissing = true)
    public CorrelationIdProvider correlationIdProvider(@Autowired final Tracer tracer) {
        return new CorrelationIdProvider() {

            @Override
            public String getCorrelationId() {
                Span currentSpan = tracer.currentSpan();
                if (currentSpan == null) {
                    currentSpan = tracer.nextSpan().name(defaultSpanName).start();
                }
                return currentSpan.context().traceId();
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(CorrelationIdProvider.class)
    public CorrelationIdProvider dummyCorrelationIdProvider() {
        return new CorrelationIdProvider() {

            @Override
            public String getCorrelationId() {
                return "";
            }
        };
    }
}
