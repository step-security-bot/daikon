package org.talend.daikon.spring.auth.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.talend.daikon.spring.auth.provider.Auth0AuthenticationProvider;
import org.talend.daikon.spring.auth.provider.SatAuthenticationProvider;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;

public class TalendAuthenticationManagerResolverTest {

    private static final String AUTH0_ISSUER = "https://url.auth0-dev.dumb.com/";

    private static final String IAM_ISSUER = "https://url.iam-dev.dumb.com/jwk/keys";

    private static final String INTROSPECTION_URI = "https://url.iam-dev.dumb.com/introspect";

    private TalendAuthenticationManagerResolver resolver;

    @Before
    public void setUp() {
        OAuth2ResourceServerProperties iamProperties = new OAuth2ResourceServerProperties();
        iamProperties.getOpaquetoken().setIntrospectionUri(INTROSPECTION_URI);
        iamProperties.getJwt().setJwkSetUri(IAM_ISSUER);

        OAuth2ResourceServerProperties auth0Properties = new OAuth2ResourceServerProperties();
        auth0Properties.getJwt().setIssuerUri(AUTH0_ISSUER);

        List<Auth0AuthenticationProvider> auth0AuthenticationProviders = Arrays.asList(new SatAuthenticationProvider());

        resolver = new TalendAuthenticationManagerResolver(iamProperties, auth0Properties, auth0AuthenticationProviders,
                new ConcurrentMapCache("jwk-set-store"));
    }

    @Test
    public void resolveAuth0JwtAuthenticationManager() {
        // Given
        HttpServletRequest request = mockRequestWithJwt(AUTH0_ISSUER);

        // When
        AuthenticationManager authenticationManager = resolver.resolve(request);

        // Then
        assertTrue(authenticationManager instanceof Auth0AuthenticationManager);
    }

    @Test
    public void resolveIamJwtAuthenticationManager() {
        // Given
        HttpServletRequest request = mockRequestWithJwt(IAM_ISSUER);

        // When
        AuthenticationManager authenticationManager = resolver.resolve(request);

        // Then
        assertTrue(authenticationManager instanceof ProviderManager);
        ProviderManager providerManager = (ProviderManager) authenticationManager;
        assertEquals(1, providerManager.getProviders().size());
        assertTrue(providerManager.getProviders().get(0) instanceof JwtAuthenticationProvider);
    }

    @Test
    public void resolveIamJwtAuthenticationManagerByDefault() {
        // Given
        HttpServletRequest request = mockRequestWithJwt("unrecognized-issuer");

        // When
        AuthenticationManager authenticationManager = resolver.resolve(request);

        // Then
        assertTrue(authenticationManager instanceof ProviderManager);
        ProviderManager providerManager = (ProviderManager) authenticationManager;
        assertEquals(1, providerManager.getProviders().size());
        assertTrue(providerManager.getProviders().get(0) instanceof JwtAuthenticationProvider);
    }

    @Test
    public void resolveOpaqueTokenAuthenticationManager() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer opaque-token");

        // When
        AuthenticationManager authenticationManager = resolver.resolve(request);

        // Then
        assertTrue(authenticationManager instanceof ProviderManager);
        ProviderManager providerManager = (ProviderManager) authenticationManager;
        assertEquals(1, providerManager.getProviders().size());
        assertTrue(providerManager.getProviders().get(0) instanceof OpaqueTokenAuthenticationProvider);
    }

    @Test
    public void resolveWithoutToken() {
        assertThrows(OAuth2AuthenticationException.class, () -> resolver.resolve(mock(HttpServletRequest.class)));
    }

    private static HttpServletRequest mockRequestWithJwt(String iamIssuer) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        JWTClaimsSet payload = new JWTClaimsSet.Builder().issuer(iamIssuer).audience("audience").subject("subject")
                .expirationTime(Date.from(Instant.now().minusSeconds(100))).build();
        PlainJWT plainJWT = new PlainJWT(payload);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + plainJWT.serialize());
        return request;
    }
}
