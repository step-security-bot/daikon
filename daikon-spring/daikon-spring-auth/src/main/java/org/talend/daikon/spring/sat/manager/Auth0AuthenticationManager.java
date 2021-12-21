package org.talend.daikon.spring.sat.manager;

import java.text.ParseException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.jwt.*;
import org.talend.daikon.spring.sat.exception.ServerErrorOAuth2Exception;
import org.talend.daikon.spring.sat.provider.Auth0AuthenticationProvider;

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

    private final AuthenticationManager delegate;

    private final List<Auth0AuthenticationProvider> providers;

    public Auth0AuthenticationManager(OAuth2ResourceServerProperties oauth2Properties, AuthenticationManager delegate,
            List<Auth0AuthenticationProvider> providers) {
        this.oauth2Properties = oauth2Properties;
        this.delegate = delegate;
        this.providers = providers;
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
                    LOGGER.debug("Building authentication token from headers with {}", provider.getClass().getName());
                    if (provider.mandatoryClaimsPresent(decodedJwt)) {
                        return provider.buildAuthenticationToken(decodedJwt);
                    }
                }
                // else delegate
            }
        } catch (ParseException e) {
            LOGGER.debug("Can't parse token, it is probably an opaque token");
        } catch (JwtValidationException e) {
            LOGGER.debug("Jwt Validation failed: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token: " + authentication.getPrincipal());
        } catch (BadJwtException e) {
            LOGGER.debug("Bad Jwt received: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token: " + authentication.getPrincipal());
        }

        // Otherwise delegate authentication to legacy authentication manager (case of Talend JWT or Opaque token)
        try {
            LOGGER.debug("Delegating authentication to {}", delegate.getClass().getName());
            return delegate.authenticate(authentication);
        } catch (RuntimeException ex) {
            if (ex instanceof OAuth2Exception) {
                LOGGER.debug("OAuth2Exception: {}", ex.getMessage());
                throw ex;
            } else {
                LOGGER.warn("RuntimeException: {}", ex.getMessage());
                throw new ServerErrorOAuth2Exception("Internal Server Error", ex);
            }
        }
    }

    protected JwtDecoder getJwtDecoder() {
        return JwtDecoders.fromIssuerLocation(oauth2Properties.getJwt().getIssuerUri());
    }

}
