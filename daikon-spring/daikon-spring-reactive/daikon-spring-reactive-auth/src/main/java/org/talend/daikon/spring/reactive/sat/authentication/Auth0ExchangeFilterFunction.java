package org.talend.daikon.spring.reactive.sat.authentication;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

public class Auth0ExchangeFilterFunction implements ExchangeFilterFunction {

    public static final Class<?> SERVER_WEB_EXCHANGE_KEY = ServerWebExchange.class;

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return nextRequest(request).switchIfEmpty(Mono.just(request)).flatMap(next::exchange);
    }

    private Mono<ServerWebExchange> getServerWebExchange(ContextView ctx) {
        if (!ctx.hasKey(SERVER_WEB_EXCHANGE_KEY)) {
            return Mono.empty();
        }
        return Mono.just(ctx.<ServerWebExchange> get(SERVER_WEB_EXCHANGE_KEY));
    }

    private Mono<HttpHeaders> getHeaders(ContextView ctx) {
        return getServerWebExchange(ctx).map(serverWebExchange -> serverWebExchange.getRequest().getHeaders());
    }

    public Mono<ClientRequest> nextRequest(ClientRequest request) {
        List<String> mandatoryHeaders = new SatReactiveAuthenticationProvider().getMandatoryHeaders();
        return Mono.deferContextual(Mono::just).flatMap(this::getHeaders).flatMapIterable(HttpHeaders::entrySet)
                .filter(header -> mandatoryHeaders.contains(header.getKey()))
                .collect(HttpHeaders::new, (httpHeaders, header) -> httpHeaders.addAll(header.getKey(), header.getValue()))
                .map(httpHeaders -> ClientRequest.from(request).headers(headers -> headers.putAll(httpHeaders)).build());
    }

}
