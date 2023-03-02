package org.talend.daikon.spring.audit.logs.api;

import static org.talend.daikon.security.tenant.ReactiveTenancyContextHolder.TENANCY_CONTEXT_KEY;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.talend.daikon.multitenant.context.DefaultTenancyContext;
import org.talend.daikon.multitenant.context.TenancyContext;
import org.talend.daikon.multitenant.provider.DefaultTenant;
import org.talend.daikon.security.tenant.ReactiveTenancyContextHolder;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * Inspired by {@link org.springframework.security.web.server.context.ReactorContextWebFilter}
 */
public class TenancyContextWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext().filter(c -> c.getAuthentication() != null)
                .map(SecurityContext::getAuthentication).flatMap(authentication -> chain.filter(exchange)
                        .contextWrite(c -> c.hasKey(TENANCY_CONTEXT_KEY) ? c : withTenancyContext(c, authentication)));
    }

    private Context withTenancyContext(Context mainContext, Authentication authentication) {
        return mainContext.putAll(loadTenancyContext(authentication).as(ReactiveTenancyContextHolder::withTenancyContext));
    }

    public Mono<TenancyContext> loadTenancyContext(Authentication authentication) {
        final TenancyContext tenantContext = new DefaultTenancyContext();
        Object principal = authentication.getPrincipal();
        if (principal instanceof User) {
            tenantContext.setTenant(new DefaultTenant(((User) principal).getUsername(), null));
        }
        return Mono.justOrEmpty(tenantContext);
    }
}
