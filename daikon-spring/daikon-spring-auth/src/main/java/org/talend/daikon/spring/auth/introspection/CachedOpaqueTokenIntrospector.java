package org.talend.daikon.spring.auth.introspection;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.talend.daikon.spring.auth.interceptor.IpAllowListHeaderInterceptor.X_FORWARDED_FOR;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Cache successful introspection response
 */
public class CachedOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachedOpaqueTokenIntrospector.class);

    private final OpaqueTokenIntrospector delegate;

    private final Cache cache;

    public CachedOpaqueTokenIntrospector(OpaqueTokenIntrospector delegate, Cache cache) {
        if (null == delegate) {
            throw new IllegalArgumentException("Parameter delegate is required");
        }
        if (null == cache) {
            throw new IllegalArgumentException("Parameter cache is required");
        }
        this.delegate = delegate;
        this.cache = cache;
    }

    public OAuth2AuthenticatedPrincipal introspect(String token) {
        return findInCache(token)
                .orElseGet(() -> Optional.of(delegate.introspect(token)).map(principal -> putInCache(token, principal)).get());
    }

    private Optional<OAuth2AuthenticatedPrincipal> findInCache(String token) {
        Optional<OAuth2AuthenticatedPrincipal> principal = Optional.empty();
        try {
            String key = key(token);
            Cache.ValueWrapper valueWrapper = cache.get(key);

            if (null != valueWrapper) {
                LOGGER.debug("Successfully retrieved OAuth2AuthenticatedPrincipal from cache: {}", key.hashCode());
                principal = Optional.ofNullable((OAuth2AuthenticatedPrincipal) valueWrapper.get());
            } else {
                LOGGER.debug("Key {} wasn't found in cache", key);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to get OAuth2AuthenticatedPrincipal from cache: {}", e.getMessage(), e);
        }

        return principal;
    }

    private OAuth2AuthenticatedPrincipal putInCache(String token, OAuth2AuthenticatedPrincipal principal) {
        String key = key(token);
        LOGGER.debug("Attempting to put OAuth2AuthenticatedPrincipal into cache: ({}, {})", key.hashCode(), principal);

        try {
            cache.put(key, principal);
        } catch (Exception e) {
            LOGGER.warn("Failed to put OAuth2AuthenticatedPrincipal into cache: {}", e.getMessage());
        }
        return principal;
    }

    /**
     * Compound cache key to restrict access only from the allowed IPs
     * if cache-key is token alone then users from not allowed IPs will be able to login as well
     */
    private String key(String token) {
        return token + getXFFHeaderHash();
    }

    /**
     * Instead of actual client IP we use hash of XFF header as a second part of the compound key to differentiate requests
     * from different IPs
     */
    private String getXFFHeaderHash() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(requestAttributes -> requestAttributes instanceof ServletRequestAttributes)
                .map(requestAttributes -> ((ServletRequestAttributes) requestAttributes).getRequest())
                .map(request -> isNotBlank(request.getHeader(X_FORWARDED_FOR)) ? request.getHeader(X_FORWARDED_FOR)
                        : request.getRemoteAddr())
                .filter(StringUtils::isNotBlank).map(xffHeader -> xffHeader.hashCode() + "").orElse("");
    }
}
