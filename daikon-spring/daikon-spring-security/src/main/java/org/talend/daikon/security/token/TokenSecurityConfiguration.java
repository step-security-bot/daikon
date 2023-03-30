package org.talend.daikon.security.token;

import static org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest.toAnyEndpoint;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoint;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.Filter;

/**
 * A Spring Security configuration class that ensures the Actuator (as well as paths in
 * {@link #additionalProtectedEndpoints}) are protected by a token authentication.
 *
 * @see NoConfiguredTokenFilter When configuration's token value is empty or missing.
 * @see TokenAuthenticationFilter When configuration's token value is present.
 * @see #additionalProtectedEndpoints for list of protected paths.
 */
@EnableWebSecurity
@ConditionalOnBean(PathMappedEndpoints.class)
public class TokenSecurityConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenSecurityConfiguration.class);

    @Value("${talend.security.allowPublicPrometheusEndpoint:false}")
    private boolean allowPrometheusUnauthenticatedAccess;

    @Autowired
    private PathMappedEndpoints actuatorEndpoints;

    @Autowired
    private WebEndpointProperties webEndpointProperties;

    private final List<TokenProtectedPath> additionalProtectedEndpoints;

    private final RequestMatcher protectedPaths;

    private final Filter tokenAuthenticationFilter;

    public TokenSecurityConfiguration(@Value("${talend.security.token.value:}") String token,
            @Autowired List<TokenProtectedPath> additionalProtectedEndpoints) {
        this.additionalProtectedEndpoints = additionalProtectedEndpoints;

        if (additionalProtectedEndpoints.isEmpty()) {
            protectedPaths = toAnyEndpoint();
        } else {
            // only add OrRequestMatcher on additionalProtectedEndpoints if they are some
            final AntPathRequestMatcher[] matchers = additionalProtectedEndpoints.stream() //
                    .map(TokenProtectedPath::getProtectedPath) //
                    .map(AntPathRequestMatcher::new) //
                    .toArray(AntPathRequestMatcher[]::new);
            protectedPaths = new OrRequestMatcher(new OrRequestMatcher(matchers), toAnyEndpoint());
        }

        if (StringUtils.isBlank(token)) {
            LOGGER.info("No token configured, protected endpoints are unavailable.");
            tokenAuthenticationFilter = new NoConfiguredTokenFilter(protectedPaths);
        } else {
            LOGGER.info("Configured token-based access security.");
            tokenAuthenticationFilter = new TokenAuthenticationFilter(token, protectedPaths);
        }
    }

    @Bean("securityFilterChain.tokenConfiguration")
    @Order(1)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry = http
                .securityMatcher(protectedPaths) //
                .csrf() //
                .disable() //
                .authorizeHttpRequests();
        for (TokenProtectedPath protectedPath : additionalProtectedEndpoints) {
            registry = registry.requestMatchers(protectedPath.getProtectedPath()).hasRole(TokenAuthentication.ROLE);
        }
        // Configure actuator
        final PathMappedEndpoint prometheus = actuatorEndpoints.getEndpoint(EndpointId.of("prometheus"));
        final PathMappedEndpoint health = actuatorEndpoints.getEndpoint(EndpointId.of("health"));
        final PathMappedEndpoint info = actuatorEndpoints.getEndpoint(EndpointId.of("info"));
        for (PathMappedEndpoint actuatorEndpoint : actuatorEndpoints) {
            final String rootPath = actuatorEndpoint.getRootPath();
            final boolean enforceTokenUsage;

            if (actuatorEndpoint.equals(health) || actuatorEndpoint.equals(info)) {
                enforceTokenUsage = false;
            } else if (allowPrometheusUnauthenticatedAccess && actuatorEndpoint.equals(prometheus)) {
                enforceTokenUsage = false;
                LOGGER.info("======= ALLOW UNAUTHENTICATED ACCESS TO PROMETHEUS (DEV MODE ONLY!) =======");
            } else {
                enforceTokenUsage = true;
            }

            final AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl matcher = registry
                    .requestMatchers(webEndpointProperties.getBasePath() + "/" + rootPath + "/**");
            if (enforceTokenUsage) {
                registry = matcher.hasRole(TokenAuthentication.ROLE);
            } else {
                registry = matcher.permitAll();
            }
        }
        registry.and().addFilterAfter(tokenAuthenticationFilter, BasicAuthenticationFilter.class);
        return http.build();
    }
}
