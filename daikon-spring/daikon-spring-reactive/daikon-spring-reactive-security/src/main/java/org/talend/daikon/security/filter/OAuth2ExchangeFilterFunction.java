package org.talend.daikon.security.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

/**
 * FROM :
 * https://github.com/spring-projects/spring-security/blob/master/oauth2/oauth2-resource-server/src/main/java/org/springframework/security/oauth2/server/resource/web/reactive/function/client/ServletBearerExchangeFilterFunction.java
 */
public class OAuth2ExchangeFilterFunction implements ExchangeFilterFunction {

    // FROM org.springframework.security.core.context.ReactiveSecurityContextHolder
    public static final Class<?> SECURITY_CONTEXT_KEY = SecurityContext.class;

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return getAuthentication().map(token -> setAuthorizationToken(request, token)).defaultIfEmpty(request)
                .flatMap(next::exchange);
    }

    private Mono<Authentication> getAuthentication() {
        // @formatter:off
        return Mono.deferContextual(ctx -> getSecurityContext(ctx).map(SecurityContext::getAuthentication));
        // @formatter:on
    }

    private Mono<SecurityContext> getSecurityContext(ContextView ctx) {
        // NOTE: SecurityReactorContextConfiguration.SecurityReactorContextSubscriber adds this key
        if (!ctx.hasKey(SECURITY_CONTEXT_KEY)) {
            return Mono.empty();
        }
        return ctx.<Mono<SecurityContext>> get(SECURITY_CONTEXT_KEY);
    }

    private ClientRequest setAuthorizationToken(ClientRequest request, Authentication authentication) {
        return ClientRequest.from(request).header(HttpHeaders.AUTHORIZATION, generateAuthorizationToken(authentication)).build();
    }

    String generateAuthorizationToken(Authentication authentication) {
        if (authentication.getCredentials() instanceof AbstractOAuth2Token) {
            return "Bearer " + ((AbstractOAuth2Token) authentication.getCredentials()).getTokenValue();
        }
        return null;
    }
}
