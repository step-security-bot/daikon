package org.talend.daikon.spring.reactive.sat.authentication;

import java.text.ParseException;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;

import com.nimbusds.jwt.JWTParser;

import reactor.core.publisher.Mono;

/**
 * The Auth0 reactive authentication manager handles authentication in case of Auth0 JWT usage in a reactive application.
 * Validates headers populated by passthrough-auth or Auth0 JWT itself issued for service account
 * In case no headers found and token is not SAT delegates to provided AuthenticationManager delegate
 */
public class Auth0ReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(Auth0ReactiveAuthenticationManager.class);

    private final OAuth2ResourceServerProperties oauth2Properties;

    private final List<Auth0ReactiveAuthenticationProvider> providers;

    public Auth0ReactiveAuthenticationManager(OAuth2ResourceServerProperties oauth2Properties,
            List<Auth0ReactiveAuthenticationProvider> providers) {
        Assert.notNull(oauth2Properties.getJwt().getIssuerUri(),
                "spring.security.oauth2.resourceserver.jwt.issuer-uri must be set.");
        this.oauth2Properties = oauth2Properties;
        this.providers = providers;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) throws AuthenticationException {
        return Mono.deferContextual(ctx -> {
            try {
                String token = (String) authentication.getPrincipal();
                JWTParser.parse(token);
                ServerHttpRequest request = ctx.get(ServerWebExchange.class).getRequest();
                // If the user is already authenticated by passthrough service then request contains appropriate headers,
                // the JWT is not decoded and the principal is computed from the headers
                for (Auth0ReactiveAuthenticationProvider provider : providers) {
                    if (provider.isAlreadyAuthenticated(request)) {
                        LOGGER.debug("Building authentication token from headers with {}", provider.getClass().getName());
                        return Mono.just(provider.buildAuthenticationToken(null, request));
                    }
                }

                // Otherwise, the JWT is decoded and the principal is computed from its claims
                Jwt decodedJwt = getJwtDecoder().decode(token);
                LOGGER.debug("Checking if request can be authenticated with one of Auth0AuthenticationProviders");
                for (Auth0ReactiveAuthenticationProvider provider : providers) {
                    LOGGER.debug("Building authentication token from headers with {}", provider.getClass().getName());
                    if (provider.mandatoryClaimsPresent(decodedJwt)) {
                        return Mono.just(provider.buildAuthenticationToken(decodedJwt, request));
                    }
                }
            } catch (ParseException e) {
                LOGGER.debug("Can't parse token, it is probably an opaque token");
                throw new InvalidBearerTokenException("Can't parse token, it is probably an opaque token", e);
            } catch (JwtValidationException e) {
                LOGGER.debug("Jwt Validation failed: {}", e.getMessage());
                throw new InvalidBearerTokenException("JWT validation failed: " + authentication.getPrincipal(), e);
            } catch (BadJwtException e) {
                LOGGER.debug("Bad Jwt received: {}", e.getMessage());
                throw new InvalidBearerTokenException("Bad JWT received: " + authentication.getPrincipal(), e);
            } catch (NoSuchElementException e) {
                LOGGER.debug("Request is not available in the context.");
                throw new InvalidBearerTokenException("Request is not available in the context.", e);
            }
            throw new InvalidBearerTokenException("Cannot authenticate with token: " + authentication.getPrincipal());
        });

    }

    protected JwtDecoder getJwtDecoder() {
        return JwtDecoders.fromIssuerLocation(oauth2Properties.getJwt().getIssuerUri());
    }

}
