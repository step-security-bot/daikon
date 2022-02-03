package org.talend.daikon.spring.reactive.sat.authentication;

import java.text.ParseException;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.talend.daikon.spring.reactive.sat.config.ReactiveAuthenticationManagerFactory;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

import reactor.core.publisher.Mono;

public class TalendReactiveAuthenticationManagerResolver implements ReactiveAuthenticationManagerResolver<ServerWebExchange> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TalendReactiveAuthenticationManagerResolver.class);

    private final OAuth2ResourceServerProperties oauth2Properties;

    private final ReactiveAuthenticationManagerFactory reactiveAuthenticationManagerFactory;

    public TalendReactiveAuthenticationManagerResolver(OAuth2ResourceServerProperties oauth2Properties,
            ReactiveAuthenticationManagerFactory reactiveAuthenticationManagerFactory) {
        Assert.notNull(oauth2Properties, "spring.security.oauth2.resourceserver must be set");
        Assert.notNull(reactiveAuthenticationManagerFactory, "ReactiveAuthenticationManagerFactory bean missing from context");
        this.oauth2Properties = oauth2Properties;
        this.reactiveAuthenticationManagerFactory = reactiveAuthenticationManagerFactory;
    }

    @Override
    public Mono<ReactiveAuthenticationManager> resolve(ServerWebExchange exchange) {
        return Mono.just(exchange.getRequest().getHeaders()).mapNotNull(headers -> headers.getFirst(HttpHeaders.AUTHORIZATION))
                .filter(authorizationHeaderHasValue()).map(authorizationHeader -> authorizationHeader.replace("Bearer ", ""))
                .flatMap(token -> {
                    try {
                        JWT jwt = JWTParser.parse(token);
                        String issuer = jwt.getJWTClaimsSet().getIssuer();
                        if (issuer.equals(oauth2Properties.getJwt().getIssuerUri())) {
                            LOGGER.debug(
                                    "JWT token is issued by the same issuer as the OAuth2 token: trying to authenticate with the SAT");
                            return reactiveAuthenticationManagerFactory.getAuth0ReactiveAuthenticationManager();
                        }
                        LOGGER.debug(
                                "JWT token is issued by a different issuer than the OAuth2 token: trying to authenticate with JWK");
                        return reactiveAuthenticationManagerFactory.getJwtReactiveAuthenticationManager();
                    } catch (ParseException e) {
                        LOGGER.debug("Cannot parse the token: trying to authenticate with Opaque manager");
                        return reactiveAuthenticationManagerFactory.getOpaqueReactiveAuthenticationManager();
                    }
                })
                .switchIfEmpty(Mono.error(new OAuth2AuthenticationException(new OAuth2Error("No authentication manager found"))));
    }

    private Predicate<String> authorizationHeaderHasValue() {
        return header -> {
            if (!StringUtils.hasText(header)) {
                LOGGER.debug("No Authorization header found");
                return false;
            }
            return true;
        };
    }
}
