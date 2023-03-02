// ============================================================================
//
// Copyright (C) 2006-2023 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.daikon.spring.audit.logs.service;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.talend.daikon.exception.TalendRuntimeException;
import org.talend.daikon.exception.error.ErrorCode;
import org.talend.daikon.multitenant.context.TenancyContext;
import org.talend.daikon.multitenant.core.Tenant;
import org.talend.daikon.security.tenant.ReactiveTenancyContextHolder;
import org.talend.daikon.spring.audit.logs.api.GenerateAuditLog;
import org.talend.daikon.spring.audit.logs.error.AuditLogsErrorCode;
import org.talend.daikon.spring.audit.logs.model.BodyCaptureExchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class AuditLogCustomWebFilter implements WebFilter {

    private final AuditLogSender auditLogSender;

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Override
    public Mono<Void> filter(final ServerWebExchange serverWebExchange, final WebFilterChain chain) {
        return getEndpointMethod(serverWebExchange, chain).zipWith(getTenant()).flatMap(methodAndTenant -> {
            if (methodAndTenant.getT1().getAnnotation(GenerateAuditLog.class) != null) {
                BodyCaptureExchange bodyCaptureExchange = new BodyCaptureExchange(serverWebExchange);
                return chain.filter(bodyCaptureExchange)
                        .doFinally(unused -> sendAuditLog(bodyCaptureExchange, methodAndTenant.getT1(), methodAndTenant.getT2()));
            }
            return chain.filter(serverWebExchange);
        }).onErrorResume(throwable -> {
            if (throwable instanceof AuditLogsWebFilterException) {
                return chain.filter(serverWebExchange);
            }
            return Mono.error(throwable);
        });
    }

    private void sendAuditLog(final BodyCaptureExchange bodyCaptureExchange, final Method method, String tenant) {
        GenerateAuditLog annotation = method.getAnnotation(GenerateAuditLog.class);

        log.debug("request: {}", bodyCaptureExchange.getRequest().getFullBody());
        log.debug("response: {}", bodyCaptureExchange.getResponse().getFullBody());

        auditLogSender.sendAuditLog(tenant, bodyCaptureExchange.getRequest(), bodyCaptureExchange.getRequest().getFullBody(),
                Objects.requireNonNull(bodyCaptureExchange.getResponse().getStatusCode()).value(),
                bodyCaptureExchange.getResponse().getFullBody(), annotation);
    }

    private Mono<Method> getEndpointMethod(ServerWebExchange serverWebExchange, WebFilterChain chain) {
        return requestMappingHandlerMapping.getHandler(serverWebExchange).cast(HandlerMethod.class).map(HandlerMethod::getMethod)
                .switchIfEmpty(Mono.error(new AuditLogsWebFilterException(AuditLogsErrorCode.METHOD_NOT_HANDLED)))
                .doOnError(e -> log.debug("Skipping audit-log filter because endpoint is not handled. {}", e.getMessage()))
                .doOnSuccess(
                        o -> log.debug("Annotations found on method {}: {}", o.getName(), Arrays.toString(o.getAnnotations())));
    }

    private Mono<String> getTenant() {
        return ReactiveTenancyContextHolder.getContext().map(TenancyContext::getTenant).map(Tenant::getIdentity)
                .switchIfEmpty(Mono.error(new AuditLogsWebFilterException(AuditLogsErrorCode.TENANT_UNAVAILABLE)))
                .doOnError(e -> log.debug("Skipping audit-log filter because tenant is not available. {}", e.getMessage()))
                .cast(String.class);
    }

    static class AuditLogsWebFilterException extends TalendRuntimeException {

        public AuditLogsWebFilterException(ErrorCode code) {
            super(code);
        }
    }
}
