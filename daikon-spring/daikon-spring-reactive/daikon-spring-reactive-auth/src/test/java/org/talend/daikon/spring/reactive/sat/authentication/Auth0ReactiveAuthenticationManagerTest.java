package org.talend.daikon.spring.reactive.sat.authentication;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.server.ServerWebExchange;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class Auth0ReactiveAuthenticationManagerTest {

    private static final String ISSUER = "https://url.talend-dev.dumb.com/";

    private Auth0ReactiveAuthenticationManager authenticationManager;

    private ServerWebExchange serverWebExchange;

    @BeforeEach
    public void setUp() {
        SatReactiveAuthenticationProvider satAuthenticationProvider = new SatReactiveAuthenticationProvider();
        Auth0ReactiveAuthenticationManager authenticationManager = new Auth0ReactiveAuthenticationManager(mockProperties(),
                asList(satAuthenticationProvider));
        this.authenticationManager = Mockito.spy(authenticationManager);
        mockServer();
    }

    private void mockServer() {
        this.serverWebExchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = MockServerHttpRequest.get("/").build();
        when(serverWebExchange.getRequest()).thenReturn(request);
    }

    private OAuth2ResourceServerProperties mockProperties() {
        OAuth2ResourceServerProperties properties = mock(OAuth2ResourceServerProperties.class);
        OAuth2ResourceServerProperties.Jwt jwt = new OAuth2ResourceServerProperties.Jwt();
        jwt.setIssuerUri(ISSUER);
        Mockito.when(properties.getJwt()).thenReturn(jwt);
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

        // Then
        StepVerifier
                .create(authenticationManager.authenticate(authentication)
                        .contextWrite(ctx -> ctx.put(ServerWebExchange.class, serverWebExchange)))
                .expectErrorMatches(throwable -> throwable instanceof InvalidBearerTokenException
                        && ((InvalidBearerTokenException) throwable).getError().getDescription().contains("JWT validation failed")
                        && ((InvalidBearerTokenException) throwable).getError().getDescription().contains(jwtString))
                .verify();
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

        // Then
        StepVerifier
                .create(authenticationManager.authenticate(authentication)
                        .contextWrite(ctx -> ctx.put(ServerWebExchange.class, serverWebExchange)))
                .expectErrorMatches(throwable -> throwable instanceof InvalidBearerTokenException
                        && ((InvalidBearerTokenException) throwable).getError().getDescription().contains("Bad JWT received")
                        && ((InvalidBearerTokenException) throwable).getError().getDescription().contains(invalidSignatureJwt))
                .verify();
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

}
