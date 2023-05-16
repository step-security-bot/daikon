package org.talend.daikon.spring.ccf.context.configuration;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.talend.daikon.spring.ccf.context.TenantParameterExtractor;
import org.talend.daikon.spring.ccf.context.TenantParameterExtractorImpl;

import lombok.extern.slf4j.Slf4j;

@EnableCaching
@EnableScheduling
@Configuration
@Slf4j
public class M2MFunctionalContextConfig {

    @Bean
    @ConditionalOnMissingBean(TenantParameterExtractor.class)
    public TenantParameterExtractor defaultRequestParamExtractor() {
        return new TenantParameterExtractorImpl();
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager defaultCacheManager(@Value("${spring.ccf.context.cache.name:ccfScimCache}") String cacheName) {
        return new ConcurrentMapCacheManager(cacheName);
    }

    @CacheEvict(allEntries = true, cacheNames = { "${spring.ccf.context.cache.name:ccfScimCache}" })
    @Scheduled(fixedDelayString = "${spring.ccf.context.cache.ttl:5}", timeUnit = TimeUnit.DAYS)
    public void reportCacheEvict() {
        log.info("Flushing CCF Scim Cache");
    }
}
