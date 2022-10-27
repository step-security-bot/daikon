package org.talend.daikon.spring.auth.manager;

import static org.springframework.util.StringUtils.hasText;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.cache.Cache;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.SupplierJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.client.RestTemplate;
import org.talend.daikon.spring.auth.common.model.userdetails.AuthUserDetails;
import org.talend.daikon.spring.auth.common.model.userdetails.UserDetailsConverter;
import org.talend.daikon.spring.auth.interceptor.IpAllowListHeaderInterceptor;
import org.talend.daikon.spring.auth.introspection.AuthUserDetailsConverterIntrospector;
import org.talend.daikon.spring.auth.introspection.CachedOpaqueTokenIntrospector;
import org.talend.daikon.spring.auth.introspection.factory.UserDetailsIntrospectorFactory;
import org.talend.daikon.spring.auth.provider.Auth0AuthenticationProvider;

public class AuthenticationManagerFactory {

    // Auth0 JWT
    public static AuthenticationManager auth0JwtAuthenticationManager(OAuth2ResourceServerProperties auth0Oauth2Properties,
            List<Auth0AuthenticationProvider> providers, Cache jwkSetCache) {
        return new Auth0AuthenticationManager(auth0Oauth2Properties, providers, jwkSetCache);
    }

    // IAM JWT
    public static AuthenticationManager iamJwtAuthenticationManager(OAuth2ResourceServerProperties iamOauth2Properties,
            Cache jwkSetCache) {
        if (hasText(iamOauth2Properties.getJwt().getJwkSetUri())) {
            // supplier is provided, otherwise unit tests are failing when trying to query the URL
            SupplierJwtDecoder jwtDecoder = new SupplierJwtDecoder(
                    () -> NimbusJwtDecoder.withJwkSetUri(iamOauth2Properties.getJwt().getJwkSetUri()).cache(jwkSetCache).build());
            JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
            jwtAuthenticationProvider.setJwtAuthenticationConverter(getJwtAuthenticationConverter());

            return new ProviderManager(jwtAuthenticationProvider);
        }

        throw new IllegalArgumentException(
                "Property spring.security.oauth2.resourceserver.iam.jwt.jwk-set-uri must be present in application properties");
    }

    /**
     * Converts jwt into {@link AuthUserDetails} object
     */
    private static Converter<Jwt, AbstractAuthenticationToken> getJwtAuthenticationConverter() {
        return jwt -> {
            OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, jwt.getTokenValue(),
                    jwt.getIssuedAt(), jwt.getExpiresAt());
            AuthUserDetails principal = UserDetailsConverter.convert(jwt.getClaims());
            return new BearerTokenAuthentication(principal, accessToken, principal.getAuthorities());
        };
    }

    // PAT
    public static AuthenticationManager opaqueTokenAuthenticationManager(OAuth2ResourceServerProperties iamOauth2Properties,
            Cache patIntrospectionCache, UserDetailsIntrospectorFactory userDetailsIntrospectorFactory) {
        if (!hasText(iamOauth2Properties.getOpaquetoken().getIntrospectionUri())) {
            throw new IllegalArgumentException("Property spring.security.oauth2.resourceserver.iam.opaque-token."
                    + "introspection-uri must be present in application properties");
        }

        String introspectionUri = iamOauth2Properties.getOpaquetoken().getIntrospectionUri();
        String clientId = Optional.ofNullable(iamOauth2Properties.getOpaquetoken().getClientId()).orElse("");
        String clientSecret = Optional.ofNullable(iamOauth2Properties.getOpaquetoken().getClientSecret()).orElse("");

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(clientId, clientSecret));
        restTemplate.getInterceptors().add(new IpAllowListHeaderInterceptor());

        NimbusOpaqueTokenIntrospector delegate = new NimbusOpaqueTokenIntrospector(introspectionUri, restTemplate);
        OpaqueTokenIntrospector converterIntrospector = userDetailsIntrospectorFactory.build(delegate);

        if (null != patIntrospectionCache) {
            return opaqueTokenProviderManager(new CachedOpaqueTokenIntrospector(converterIntrospector, patIntrospectionCache));
        } else {
            return opaqueTokenProviderManager(converterIntrospector);
        }
    }

    private static ProviderManager opaqueTokenProviderManager(OpaqueTokenIntrospector introspector) {
        return new ProviderManager(new OpaqueTokenAuthenticationProvider(introspector));
    }

}
