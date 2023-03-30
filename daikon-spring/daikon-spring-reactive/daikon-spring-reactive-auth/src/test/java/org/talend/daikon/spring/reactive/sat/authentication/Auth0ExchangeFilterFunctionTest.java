package org.talend.daikon.spring.reactive.sat.authentication;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.server.ServerWebExchange;

import reactor.test.StepVerifier;

public class Auth0ExchangeFilterFunctionTest {

    private final static String DEFAULT_URL = "/";

    private final static String USELESS_HEADER_KEY = "Useless-Header";

    private ServerWebExchange serverWebExchange = mock(ServerWebExchange.class);

    private List<String> mandatoryHeaders;

    private void mockServer(ServerHttpRequest request) {
        reset(serverWebExchange);
        when(serverWebExchange.getRequest()).thenReturn(request);
    }

    @BeforeEach
    public void setUp() {
        this.mandatoryHeaders = new SatReactiveAuthenticationProvider().getMandatoryHeaders();
    }

    @Test
    public void nextRequestShouldContainIncomingMandatoryHeaders() {
        // Given
        HttpHeaders mandatoryHttpHeaders = new HttpHeaders();
        for (String header : this.mandatoryHeaders) {
            mandatoryHttpHeaders.add(header, "value");
        }
        ServerHttpRequest serverRequest = MockServerHttpRequest.get(DEFAULT_URL).headers(mandatoryHttpHeaders)
                .header(USELESS_HEADER_KEY, "useless-value").build();
        mockServer(serverRequest);
        ClientRequest clientRequest = ClientRequest.create(HttpMethod.GET, URI.create(DEFAULT_URL)).build();
        Auth0ExchangeFilterFunction function = new Auth0ExchangeFilterFunction();

        // When Then
        StepVerifier
                .create(function.nextRequest(clientRequest)
                        .contextWrite(ctx -> ctx.put(ServerWebExchange.class, serverWebExchange)))
                .expectNextMatches(nextRequest -> {
                    for (String header : this.mandatoryHeaders) {
                        if (!nextRequest.headers().containsKey(header)) {
                            return false;
                        }
                    }
                    return !nextRequest.headers().containsKey(USELESS_HEADER_KEY);
                }).verifyComplete();
    }
}
