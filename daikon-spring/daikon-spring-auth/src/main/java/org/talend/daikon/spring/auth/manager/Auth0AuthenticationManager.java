package org.talend.daikon.spring.auth.manager;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.cache.Cache;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.util.StringUtils;
import org.talend.daikon.spring.auth.provider.Auth0AuthenticationProvider;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

/**
 * The Auth0 authentication manager handles authentication in case of Auth0 JWT usage
 * Validates headers populated by passthrough-auth or Auth0 JWT itself issued for service account
 * In case no headers found and token is not SAT delegates to provided AuthenticationManager delegate
 */
public class Auth0AuthenticationManager implements AuthenticationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(Auth0AuthenticationManager.class);

    private final OAuth2ResourceServerProperties oauth2Properties;

    private final List<Auth0AuthenticationProvider> providers;

    private final NimbusJwtDecoder jwtDecoder;

    public Auth0AuthenticationManager(OAuth2ResourceServerProperties oauth2Properties,
            List<Auth0AuthenticationProvider> providers, Cache jwkSetCache) {
        if (null == providers || providers.isEmpty()) {
            throw new IllegalArgumentException("Auth0 authentication providers list cannot be empty");
        }
        if (!StringUtils.hasText(oauth2Properties.getJwt().getIssuerUri())) {
            throw new IllegalArgumentException("Auth0 issuer uri must not be null. "
                    + "Please set spring.security.oauth2.resourceserver.auth0.jwt.issuer-uri "
                    + "property in your application.yml");
        }
        this.oauth2Properties = oauth2Properties;
        this.providers = providers;
        this.jwtDecoder = NimbusJwtDecoder.withJwkSetUri(Optional.of(oauth2Properties.getJwt().getIssuerUri())
                .map(v -> v.endsWith("/") ? v : v + "/").map(v -> v + ".well-known/jwks.json").get()).cache(jwkSetCache).build();
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        // Handle authentication in case of Auth0 JWT usage
        try {
            String token = (String) authentication.getPrincipal();
            JWT jwt = JWTParser.parse(token);
            String issuer = jwt.getJWTClaimsSet().getIssuer();

            if (issuer.equals(oauth2Properties.getJwt().getIssuerUri())) {
                // If the user is already authenticated by passthrough service then request contains appropriate headers,
                // the JWT is not decoded and the principal is computed from the headers
                LOGGER.debug("Checking if request contains authentication headers");
                for (Auth0AuthenticationProvider provider : providers) {
                    if (provider.isAlreadyAuthenticated()) {
                        LOGGER.debug("Building authentication token from headers with {}", provider.getClass().getName());
                        return provider.buildAuthenticationToken(null);
                    }
                }

                // Otherwise, the JWT is decoded and the principal is computed from its claims
                JwtDecoder jwtDecoder = getJwtDecoder();
                Jwt decodedJwt = jwtDecoder.decode(token);
                LOGGER.debug("Checking if request can be authenticated with one of Auth0AuthenticationProviders");
                for (Auth0AuthenticationProvider provider : providers) {
                    LOGGER.debug("Building authentication token from decodedJwt with {}", provider.getClass().getName());
                    if (provider.mandatoryClaimsPresent(decodedJwt)) {
                        return provider.buildAuthenticationToken(decodedJwt);
                    }
                }
            }
        } catch (ParseException e) {
            LOGGER.warn("Can't parse token, it is probably an opaque token");
        } catch (JwtValidationException e) {
            LOGGER.debug("Jwt Validation failed: {}", e.getMessage());
            throw new InvalidBearerTokenException("Invalid token: " + authentication.getPrincipal());
        } catch (BadJwtException e) {
            LOGGER.debug("Bad Jwt received: {}", e.getMessage());
            throw new InvalidBearerTokenException("Invalid token: " + authentication.getPrincipal());
        }

        LOGGER.debug("Request cannot be authenticated with Auth0AuthenticationManager");
        return null;
    }

    protected JwtDecoder getJwtDecoder() {
        return jwtDecoder;
    }

}
