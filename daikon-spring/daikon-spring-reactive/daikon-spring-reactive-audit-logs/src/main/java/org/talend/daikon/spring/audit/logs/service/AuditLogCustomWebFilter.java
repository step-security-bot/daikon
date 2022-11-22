package org.talend.daikon.spring.audit.logs.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.handler.DefaultWebFilterChain;
import org.talend.daikon.multitenant.context.TenancyContext;
import org.talend.daikon.multitenant.core.Tenant;
import org.talend.daikon.security.tenant.ReactiveTenancyContextHolder;
import org.talend.daikon.spring.audit.logs.api.GenerateAuditLog;
import org.talend.daikon.spring.audit.logs.model.BodyCaptureExchange;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.lang.reflect.Method;
import java.util.Objects;

@AllArgsConstructor
@Slf4j
public class AuditLogCustomWebFilter implements WebFilter {

    private final AuditLogSender auditLogSender;

    public Mono<Void> filter(final ServerWebExchange serverWebExchange, final WebFilterChain chain) {
        return Mono.just(chain).cast(DefaultWebFilterChain.class).map(DefaultWebFilterChain::getHandler)
                .cast(DispatcherHandler.class).mapNotNull(DispatcherHandler::getHandlerMappings)
                .flatMapIterable(handlerMappings -> handlerMappings)
                .filter(handlerMapping -> handlerMapping instanceof RequestMappingHandlerMapping).next()
                .cast(RequestMappingHandlerMapping.class)
                .flatMap(handlerMapping -> handlerMapping.getHandlerInternal(serverWebExchange)).map(HandlerMethod::getMethod)
                .zipWith(getTenant()).flatMap(methodTenantTuple -> {
                    if (methodTenantTuple.getT1().getAnnotation(GenerateAuditLog.class) != null) {
                        return sendAuditLog(chain, serverWebExchange, methodTenantTuple);
                    } else {
                        return chain.filter(serverWebExchange);
                    }
                });
    }

    private Mono<Void> sendAuditLog(final WebFilterChain chain, final ServerWebExchange serverWebExchange,
            final Tuple2<Method, String> methodTenantTuple) {
        BodyCaptureExchange bodyCaptureExchange = new BodyCaptureExchange(serverWebExchange);
        return chain.filter(bodyCaptureExchange).doOnSuccess(unused -> sendAuditLog(bodyCaptureExchange, methodTenantTuple))
                .doOnError(throwable -> sendAuditLog(bodyCaptureExchange, methodTenantTuple));
    }

    private void sendAuditLog(final BodyCaptureExchange bodyCaptureExchange, final Tuple2<Method, String> methodTenantTuple) {
        GenerateAuditLog annotation = methodTenantTuple.getT1().getAnnotation(GenerateAuditLog.class);

        log.debug("request: {}", bodyCaptureExchange.getRequest().getFullBody());
        log.debug("response: {}", bodyCaptureExchange.getResponse().getFullBody());

        auditLogSender.sendAuditLog(methodTenantTuple.getT2(), bodyCaptureExchange.getRequest(),
                bodyCaptureExchange.getRequest().getFullBody(),
                Objects.requireNonNull(bodyCaptureExchange.getResponse().getStatusCode()).value(),
                bodyCaptureExchange.getResponse().getFullBody(), annotation);
    }

    private Mono<String> getTenant() {
        return ReactiveTenancyContextHolder.getContext().map(TenancyContext::getTenant).map(Tenant::getIdentity)
                .switchIfEmpty(Mono.error(new RuntimeException("TenantId is not available in the context"))).cast(String.class);
    }

}
