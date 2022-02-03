package org.talend.daikon.security.tenant;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.talend.daikon.multitenant.context.TenancyContext;
import org.talend.daikon.spring.auth.common.model.userdetails.AuthUserDetails;

public class TenancyContextWebFilterTest {

    @Test
    public void tenancyContextShouldBeLoadedWithJwt() {
        TenancyContextWebFilter tenancyContextWebFilter = new TenancyContextWebFilter();

        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("entitlements", "FAKE")
                .claim("tenant_id", "FakeTenantId").build();

        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt);

        TenancyContext tenancyContext = tenancyContextWebFilter.loadTenancyContext(jwtAuthenticationToken).block();
        assertEquals("FakeTenantId", tenancyContext.getTenant().getIdentity());
    }

    @Test
    public void tenancyContextShouldBeLoadedWithAccessToken() {
        TenancyContextWebFilter tenancyContextWebFilter = new TenancyContextWebFilter();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("tenant_id", "FakeTenantId");
        OAuth2AuthenticatedPrincipal principal = new OAuth2IntrospectionAuthenticatedPrincipal(attributes, null);

        Authentication authentication = new TestingAuthenticationToken(principal, null);

        TenancyContext tenancyContext = tenancyContextWebFilter.loadTenancyContext(authentication).block();
        assertEquals("FakeTenantId", tenancyContext.getTenant().getIdentity());
    }

    @Test
    public void tenancyContextShouldBeLoadedWithAuth0Token() {
        TenancyContextWebFilter tenancyContextWebFilter = new TenancyContextWebFilter();
        AuthUserDetails principal = new AuthUserDetails("username", "password", Collections.emptyList());
        principal.setTenantId("FakeTenantId");

        Authentication authentication = new TestingAuthenticationToken(principal, null);

        TenancyContext tenancyContext = tenancyContextWebFilter.loadTenancyContext(authentication).block();
        assertEquals("FakeTenantId", tenancyContext.getTenant().getIdentity());
    }
}
