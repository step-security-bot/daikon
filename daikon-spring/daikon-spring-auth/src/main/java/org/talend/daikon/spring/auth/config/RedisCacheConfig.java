package org.talend.daikon.spring.auth.config;

import java.time.Duration;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.talend.daikon.spring.auth.cache.CacheExceptionHandlerWrapper;

@ConditionalOnClass(RedisCacheManager.RedisCacheManagerBuilder.class)
@Configuration
public class RedisCacheConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheConfig.class);

    @Value("${spring.security.oauth2.resourceserver.iam.opaque-token.cache-name:oauthTokenInfoCache}")
    private String patIntrospectionCacheName;

    @Value("${spring.security.oauth2.resourceserver.iam.opaque-token.cache-redis-ttl:2m}")
    private Duration ttl;

    @Bean
    public RedisCacheManagerBuilderCustomizer patIntrospectionCacheTtlCustomizer() {
        return (builder) -> {
            LOGGER.info("Applying Redis cache customizer with ttl: {}", ttl);
            builder.withCacheConfiguration(patIntrospectionCacheName, RedisCacheConfiguration.defaultCacheConfig().entryTtl(ttl));
        };
    }

    public static Function<Cache, Cache> wrapRedisCacheWithExceptionHandler() {
        return delegate -> delegate.getClass().getName().toLowerCase().contains("redis")
                ? (Cache) new CacheExceptionHandlerWrapper(delegate)
                : delegate;
    }
}
