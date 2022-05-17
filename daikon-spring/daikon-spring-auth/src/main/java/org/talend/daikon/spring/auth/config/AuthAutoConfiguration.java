package org.talend.daikon.spring.auth.config;

import static org.talend.daikon.spring.auth.config.RedisCacheConfig.wrapRedisCacheWithExceptionHandler;

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestTemplate;
import org.talend.daikon.multitenant.provider.TenantProvider;
import org.talend.daikon.multitenant.web.TenantIdentificationStrategy;
import org.talend.daikon.spring.auth.exception.TalendBearerTokenAuthenticationEntryPoint;
import org.talend.daikon.spring.auth.interceptor.BearerTokenInterceptor;
import org.talend.daikon.spring.auth.interceptor.IpAllowListHeaderInterceptor;
import org.talend.daikon.spring.auth.manager.AuthenticationManagerFactory;
import org.talend.daikon.spring.auth.manager.TalendAuthenticationManagerResolver;
import org.talend.daikon.spring.auth.multitenant.AccountSecurityContextIdentificationStrategy;
import org.talend.daikon.spring.auth.multitenant.UserDetailsTenantProvider;
import org.talend.daikon.spring.auth.provider.Auth0AuthenticationProvider;
import org.talend.daikon.spring.auth.provider.SatAuthenticationProvider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enables Service Account Token authentication
 */
@Configuration
@ConditionalOnClass(value = { Jwt.class, OAuth2ResourceServerProperties.class })
@Import(RedisCacheConfig.class)
@EnableConfigurationProperties(OAuth2ResourceServerProperties.class)
public class AuthAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthAutoConfiguration.class);

    @Value("${spring.security.oauth2.resourceserver.jwk-set-cache-name:jwk-set-cache}")
    private String jwkSetCacheName;

    @Value("${spring.security.oauth2.resourceserver.iam.opaque-token.cache-name:oauthTokenInfoCache}")
    private String patIntrospectionCacheName;

    @Value("${spring.security.oauth2.resourceserver.iam.opaque-token.cache-enabled:true}")
    private boolean patIntrospectionCacheEnabled;

    @Bean
    public RestTemplate oauth2RestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new BearerTokenInterceptor());
        restTemplate.getInterceptors().add(new IpAllowListHeaderInterceptor());
        return restTemplate;
    }

    @Bean
    public AuthenticationManagerResolver<HttpServletRequest> authManagerResolver(
            @Qualifier("iamOauth2Properties") OAuth2ResourceServerProperties iamOauth2Properties,
            @Qualifier("auth0Oauth2Properties") OAuth2ResourceServerProperties auth0Oauth2Properties,
            List<Auth0AuthenticationProvider> auth0AuthenticationProviders, Optional<CacheManager> cacheManager) {

        cacheManager.ifPresent(manager -> LOGGER.info("Cache manager {} is found", manager.getClass().getName()));

        // @formatter:off
        Cache jwkSetCache = cacheManager
                .filter(manager -> manager.getCacheNames().contains(jwkSetCacheName)) // otherwise Redis creates it "InFlight"
                .map(manager -> manager.getCache(jwkSetCacheName))
                .map(cache -> {
                    LOGGER.info("{} is used for jwkSet cache {}", cacheManager.get().getClass().getName(), jwkSetCacheName);
                    return cache;
                })
                .map(wrapRedisCacheWithExceptionHandler()) // only for redis cache (TPSVC-18791)
                .orElseGet(() -> {
                    LOGGER.info("ConcurrentMapCache is used for jwkSet cache {}", jwkSetCacheName);
                    return new ConcurrentMapCache(jwkSetCacheName);
                });

        Cache patIntrospectionCache = cacheManager.filter(ignored -> patIntrospectionCacheEnabled)
                .map(manager -> manager.getCache(patIntrospectionCacheName))
                .map(cache -> {
                    LOGGER.info("{} is used for PAT introspection cache {} : {}", cacheManager.get().getClass().getName(),
                            patIntrospectionCacheName, cache.getClass().getName());
                    return cache;
                })
                .map(wrapRedisCacheWithExceptionHandler()) // only for redis cache (TPSVC-18791)
                .orElseGet(() -> {
                    LOGGER.warn("PAT introspection cache '{}' is disabled. This may lead to performance issues. "
                            + "Please consider adding cache configuration and enabling it by setting "
                            + "spring.security.oauth2.resourceserver.iam.opaque-token.cache-enabled=true "
                            + "(Current value is '{}').", patIntrospectionCacheName, patIntrospectionCacheEnabled);
                    return null;
                });
        // @formatter:on

        AuthenticationManager auth0JwtAuthenticationManager = AuthenticationManagerFactory
                .auth0JwtAuthenticationManager(auth0Oauth2Properties, auth0AuthenticationProviders, jwkSetCache);

        AuthenticationManager iamJwtAuthenticationManager = AuthenticationManagerFactory
                .iamJwtAuthenticationManager(iamOauth2Properties, jwkSetCache);

        AuthenticationManager opaqueTokenAuthenticationManager = AuthenticationManagerFactory
                .opaqueTokenAuthenticationManager(iamOauth2Properties, patIntrospectionCache);

        return TalendAuthenticationManagerResolver.builder().auth0JwtAuthenticationManager(auth0JwtAuthenticationManager)
                .auth0IssuerUri(auth0Oauth2Properties.getJwt().getIssuerUri())
                .iamJwtAuthenticationManager(iamJwtAuthenticationManager)
                .opaqueTokenAuthenticationManager(opaqueTokenAuthenticationManager).build();
    }

    @Bean
    public Auth0AuthenticationProvider satAuthenticationProvider() {
        return new SatAuthenticationProvider();
    }

    @Bean
    @Qualifier("iamOauth2Properties")
    @ConfigurationProperties(prefix = "spring.security.oauth2.resourceserver.iam")
    public OAuth2ResourceServerProperties iamOauth2Properties() {
        return new OAuth2ResourceServerProperties();
    }

    @Bean
    @Qualifier("auth0Oauth2Properties")
    @ConfigurationProperties(prefix = "spring.security.oauth2.resourceserver.auth0")
    public OAuth2ResourceServerProperties auth0Oauth2Properties() {
        return new OAuth2ResourceServerProperties();
    }

    // multitenacy

    @Bean
    @ConditionalOnMissingBean(TenantProvider.class)
    public TenantProvider tenantProvider() {
        return new UserDetailsTenantProvider();
    }

    @Bean
    @ConditionalOnMissingBean(TenantIdentificationStrategy.class)
    public TenantIdentificationStrategy identificationStrategy() {
        return new AccountSecurityContextIdentificationStrategy();
    }

    // exception handling

    @Bean
    @ConditionalOnMissingBean(TalendBearerTokenAuthenticationEntryPoint.class)
    public TalendBearerTokenAuthenticationEntryPoint talendBearerTokenAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new TalendBearerTokenAuthenticationEntryPoint(objectMapper);
    }

    @ControllerAdvice
    @ConditionalOnProperty(value = "spring.security.oauth2.resourceserver.exception-handler.enabled", havingValue = "true", matchIfMissing = true)
    public static class AuthExceptionHandler {

        private static final Logger LOGGER = LoggerFactory.getLogger(AuthExceptionHandler.class);

        @ExceptionHandler
        public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
            if (SecurityContextHolder.getContext().getAuthentication().getClass().equals(AnonymousAuthenticationToken.class)) {
                LOGGER.debug("Handling AccessDeniedException for unauthorized request: {}", e.getMessage());
                HttpStatus status = HttpStatus.UNAUTHORIZED;
                ErrorResponse response = ErrorResponse.builder().status(status.value()).detail(status.getReasonPhrase()).build();
                return ResponseEntity.status(status).body(response);
            } else {
                LOGGER.debug("Handling AccessDeniedException with insufficient permissions: {}", e.getMessage());
                HttpStatus status = HttpStatus.FORBIDDEN;
                ErrorResponse response = ErrorResponse.builder().status(status.value()).detail(e.getMessage()).build();
                return ResponseEntity.status(status).body(response);
            }
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public static class ErrorResponse {

            private String detail;

            private int status;
        }
    }

    // log deprecated options

    @Value("${security.oauth2.resource.tokenInfoUriCache.name:#{null}}")
    private String deprecatedCacheName;

    @Value("${security.oauth2.resource.tokenInfoUriCache.enabled:#{null}}")
    private Boolean deprecatedCacheEnabled;

    @PostConstruct
    public void logDeprecatedPropertiesWarning() {
        if (deprecatedCacheName != null) {
            LOGGER.warn(
                    "Property security.oauth2.resource.tokenInfoUriCache.name is deprecated "
                            + "and its value '{}' will be ignored. "
                            + "Please use new property spring.security.oauth2.resourceserver.iam.opaque-token.cache-name",
                    deprecatedCacheName);
        }
        if (deprecatedCacheEnabled != null) {
            LOGGER.warn(
                    "Property security.oauth2.resource.tokenInfoUriCache.enabled is deprecated "
                            + "and its value '{}' will be ignored. "
                            + "Please use new property spring.security.oauth2.resourceserver.iam.opaque-token.cache-enabled",
                    deprecatedCacheEnabled);
        }
    }
}
