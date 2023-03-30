package org.talend.daikon.spring.auth.manager;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.talend.daikon.spring.auth.provider.Auth0AuthenticationProvider.CLAIM_PERMISSIONS;
import static org.talend.daikon.spring.auth.provider.Auth0AuthenticationProvider.HEADER_CLIENT_ID;
import static org.talend.daikon.spring.auth.provider.Auth0AuthenticationProvider.HEADER_PERMISSIONS;
import static org.talend.daikon.spring.auth.provider.SatAuthenticationProvider.CLAIM_SA_NAME;
import static org.talend.daikon.spring.auth.provider.SatAuthenticationProvider.CLAIM_TENANT_ID;
import static org.talend.daikon.spring.auth.provider.SatAuthenticationProvider.CLAIM_TENANT_NAME;
import static org.talend.daikon.spring.auth.provider.SatAuthenticationProvider.HEADER_SA_NAME;
import static org.talend.daikon.spring.auth.provider.SatAuthenticationProvider.HEADER_TENANT_ID;
import static org.talend.daikon.spring.auth.provider.SatAuthenticationProvider.HEADER_TENANT_NAME;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.cache.support.NoOpCache;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.talend.daikon.spring.auth.common.model.userdetails.AuthUserDetails;
import org.talend.daikon.spring.auth.provider.SatAuthenticationProvider;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@ExtendWith(MockitoExtension.class)
public class Auth0AuthenticationManagerTest {

    private static final String ISSUER = "https://url.talend-dev.dumb.com/";

    private Auth0AuthenticationManager authenticationManager;

    private SatAuthenticationProvider satAuthenticationProvider;

    @BeforeEach
    public void setUp() {
        this.satAuthenticationProvider = Mockito.spy(new SatAuthenticationProvider());
        Auth0AuthenticationManager authenticationManager = new Auth0AuthenticationManager(mockProperties(),
                asList(satAuthenticationProvider), new NoOpCache("name"));
        this.authenticationManager = Mockito.spy(authenticationManager);
    }

    @AfterEach
    public void tearDown() {
        RequestContextHolder.setRequestAttributes(null);
    }

    private OAuth2ResourceServerProperties mockProperties() {
        OAuth2ResourceServerProperties properties = new OAuth2ResourceServerProperties();
        properties.getJwt().setIssuerUri(ISSUER);
        return properties;
    }

    @Test
    public void whenExpiredTokenThenThrowException() throws Exception {
        // Given
        // build jwt
        RSAKey jwk = generateRsaKey();
        RSAPublicKey rsaKey = jwk.toRSAPublicKey();
        JWSHeader header = buildHeader(jwk);
        JWTClaimsSet payload = new JWTClaimsSet.Builder().issuer(ISSUER).audience("audience").subject("subject")
                .expirationTime(Date.from(Instant.now().minusSeconds(100))).build();
        String jwtString = getJwtString(jwk, header, payload);
        Authentication authentication = new TestingAuthenticationToken(jwtString, "");

        // mock jwt decoder with supplied key
        NimbusJwtDecoder jwtDecoder = getBuildDecoder(rsaKey);
        doReturn(jwtDecoder).when(this.authenticationManager).getJwtDecoder();

        // When
        Exception exception = assertThrows(InvalidBearerTokenException.class,
                () -> authenticationManager.authenticate(authentication));

        // Then
        assertTrue(exception.getMessage().contains("Invalid token"));
        assertTrue(exception.getMessage().contains(jwtString));
    }

    @Test
    public void whenInvalidSignatureTokenThenThrowException() throws Exception {
        // Given
        // build jwt
        RSAKey jwk = generateRsaKey();
        RSAPublicKey rsaKey = jwk.toRSAPublicKey();
        JWSHeader header = buildHeader(jwk);
        JWTClaimsSet payload = new JWTClaimsSet.Builder().issuer(ISSUER).audience("audience").subject("subject")
                .expirationTime(Date.from(Instant.now().plusSeconds(100))).build();

        // replace with invalid signature
        String[] jwtParts = getJwtString(jwk, header, payload).split("\\.");
        String invalidSignatureJwt = jwtParts[0] + "." + jwtParts[1] + "." + "invalid_signature";
        Authentication authentication = new TestingAuthenticationToken(invalidSignatureJwt, "");

        // mock jwt decoder with supplied key
        NimbusJwtDecoder jwtDecoder = getBuildDecoder(rsaKey);
        doReturn(jwtDecoder).when(this.authenticationManager).getJwtDecoder();

        // When
        Exception exception = assertThrows(InvalidBearerTokenException.class,
                () -> authenticationManager.authenticate(authentication));

        // Then
        assertTrue(exception.getMessage().contains("Invalid token"));
        assertTrue(exception.getMessage().contains(invalidSignatureJwt));
    }

    @Test
    public void whenAlreadyAuthenticatedThenProviderCalledWithoutToken() throws Exception {
        // Given
        // build jwt
        RSAKey jwk = generateRsaKey();
        JWSHeader header = buildHeader(jwk);
        JWTClaimsSet payload = new JWTClaimsSet.Builder().issuer(ISSUER).audience("audience").subject("subject")
                .expirationTime(Date.from(Instant.now().plusSeconds(100))).build();

        // mock already authenticated by Gloo request
        mockRequestHeaders(Arrays.asList(HEADER_PERMISSIONS, HEADER_TENANT_ID, HEADER_CLIENT_ID, HEADER_SA_NAME));

        Authentication authentication = new TestingAuthenticationToken(getJwtString(jwk, header, payload), "");

        // When
        Authentication result = authenticationManager.authenticate(authentication);

        // Then
        verify(satAuthenticationProvider, times(1)).isAlreadyAuthenticated();
        verify(satAuthenticationProvider, times(1)).buildAuthenticationToken(null);
        verify(satAuthenticationProvider, times(0)).buildAuthenticationToken(notNull());

        AuthUserDetails authUserDetails = (AuthUserDetails) result.getPrincipal();
        assertTrue(result.isAuthenticated());
        assertEquals("value_talend-client-id", authUserDetails.getId());
        assertEquals("value_talend-service-account-name - Service Account", authUserDetails.getUsername());
        assertEquals("value_talend-tenant-id", authUserDetails.getTenantName());
        assertEquals("value_talend-tenant-id", authUserDetails.getTenantId());
        assertEquals("value_talend-permissions",
                authUserDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(joining()));
    }

    @Test
    public void whenMandatoryClaimsPresentThenProviderCalledWithToken() throws Exception {
        // Given
        // build jwt
        RSAKey jwk = generateRsaKey();
        RSAPublicKey rsaKey = jwk.toRSAPublicKey();
        JWSHeader header = buildHeader(jwk);
        JWTClaimsSet payload = new JWTClaimsSet.Builder().issuer(ISSUER).audience("audience").subject("sa-id")
                .expirationTime(Date.from(Instant.now().plusSeconds(100))).claim(CLAIM_PERMISSIONS, "ROLE_MANAGE,USER_MANAGE")
                .claim(CLAIM_TENANT_ID, "tenant-id").claim(CLAIM_SA_NAME, "sa-name").build();

        // mock jwt decoder with supplied key
        NimbusJwtDecoder jwtDecoder = getBuildDecoder(rsaKey);
        doReturn(jwtDecoder).when(this.authenticationManager).getJwtDecoder();

        Authentication authentication = new TestingAuthenticationToken(getJwtString(jwk, header, payload), "");

        // When
        Authentication result = authenticationManager.authenticate(authentication);

        // Then
        verify(satAuthenticationProvider, times(1)).isAlreadyAuthenticated();
        verify(satAuthenticationProvider, times(0)).buildAuthenticationToken(null);
        verify(satAuthenticationProvider, times(1)).mandatoryClaimsPresent(notNull());
        verify(satAuthenticationProvider, times(1)).buildAuthenticationToken(any());

        AuthUserDetails authUserDetails = (AuthUserDetails) result.getPrincipal();
        assertTrue(result.isAuthenticated());
        assertEquals("sa-id", authUserDetails.getId());
        assertEquals("sa-name - Service Account", authUserDetails.getUsername());
        assertEquals("tenant-id", authUserDetails.getTenantName());
        assertEquals("tenant-id", authUserDetails.getTenantId());
        assertEquals("ROLE_MANAGE,USER_MANAGE",
                authUserDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(joining()));
    }

    @Test
    public void whenIssuerDoesNotMatchNullReturned() throws Exception {
        // Given
        // build jwt
        RSAKey jwk = generateRsaKey();
        JWSHeader header = buildHeader(jwk);
        JWTClaimsSet payload = new JWTClaimsSet.Builder().issuer(ISSUER + "wrong").audience("audience").subject("subject")
                .expirationTime(Date.from(Instant.now().plusSeconds(100))).build();

        Authentication authentication = new TestingAuthenticationToken(getJwtString(jwk, header, payload), "");

        // When
        Authentication result = authenticationManager.authenticate(authentication);

        // Then
        assertNull(result);
    }

    @Test
    public void authenticateFromHeadersWithTenantName() throws Exception {
        // Given
        // build jwt
        RSAKey jwk = generateRsaKey();
        JWSHeader header = buildHeader(jwk);
        JWTClaimsSet payload = new JWTClaimsSet.Builder().issuer(ISSUER).audience("audience").subject("subject")
                .expirationTime(Date.from(Instant.now().plusSeconds(100))).build();

        // mock already authenticated by Gloo request
        mockRequestHeaders(
                Arrays.asList(HEADER_PERMISSIONS, HEADER_TENANT_ID, HEADER_TENANT_NAME, HEADER_CLIENT_ID, HEADER_SA_NAME));

        Authentication authentication = new TestingAuthenticationToken(getJwtString(jwk, header, payload), "");

        // When
        Authentication result = authenticationManager.authenticate(authentication);

        // Then
        AuthUserDetails authUserDetails = (AuthUserDetails) result.getPrincipal();
        assertTrue(result.isAuthenticated());
        assertEquals("value_talend-tenant-name", authUserDetails.getTenantName());
        assertEquals("value_talend-tenant-id", authUserDetails.getTenantId());
    }

    @Test
    public void authenticateFromJwtWithTenantName() throws Exception {
        // Given
        // build jwt
        RSAKey jwk = generateRsaKey();
        RSAPublicKey rsaKey = jwk.toRSAPublicKey();
        JWSHeader header = buildHeader(jwk);
        JWTClaimsSet payload = new JWTClaimsSet.Builder().issuer(ISSUER).audience("audience").subject("sa-id")
                .expirationTime(Date.from(Instant.now().plusSeconds(100))).claim(CLAIM_PERMISSIONS, "ROLE_MANAGE,USER_MANAGE")
                .claim(CLAIM_TENANT_ID, "tenant-id").claim(CLAIM_TENANT_NAME, "tenant-name").claim(CLAIM_SA_NAME, "sa-name")
                .build();

        // mock jwt decoder with supplied key
        NimbusJwtDecoder jwtDecoder = getBuildDecoder(rsaKey);
        doReturn(jwtDecoder).when(this.authenticationManager).getJwtDecoder();

        Authentication authentication = new TestingAuthenticationToken(getJwtString(jwk, header, payload), "");

        // When
        Authentication result = authenticationManager.authenticate(authentication);

        // Then
        AuthUserDetails authUserDetails = (AuthUserDetails) result.getPrincipal();
        assertEquals("tenant-name", authUserDetails.getTenantName());
        assertEquals("tenant-id", authUserDetails.getTenantId());
    }

    private RSAKey generateRsaKey() throws Exception {
        return new RSAKeyGenerator(RSAKeyGenerator.MIN_KEY_SIZE_BITS).keyUse(KeyUse.SIGNATURE).keyID(UUID.randomUUID().toString())
                .generate();
    }

    private JWSHeader buildHeader(RSAKey jwk) {
        return new JWSHeader.Builder(JWSAlgorithm.RS256).type(JOSEObjectType.JWT).keyID(jwk.getKeyID()).build();
    }

    private String getJwtString(RSAKey jwk, JWSHeader header, JWTClaimsSet payload) throws Exception {
        SignedJWT signedJWT = new SignedJWT(header, payload);
        signedJWT.sign(new RSASSASigner(jwk));
        return signedJWT.serialize();
    }

    private NimbusJwtDecoder getBuildDecoder(RSAPublicKey rsaKey) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withPublicKey(rsaKey).build();
        OAuth2TokenValidator<Jwt> jwtValidator = JwtValidators.createDefaultWithIssuer(ISSUER);
        jwtDecoder.setJwtValidator(jwtValidator);
        return jwtDecoder;
    }

    private static void mockRequestHeaders(Collection<String> headerNames) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        headerNames.forEach(headerName -> request.addHeader(headerName, "value_" + headerName));
    }
}
