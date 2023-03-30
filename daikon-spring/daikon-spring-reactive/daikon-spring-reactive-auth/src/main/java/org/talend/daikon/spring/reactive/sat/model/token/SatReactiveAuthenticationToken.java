package org.talend.daikon.spring.reactive.sat.model.token;

import static org.talend.daikon.spring.reactive.sat.authentication.SatReactiveAuthenticationProvider.CLAIM_SA_NAME;
import static org.talend.daikon.spring.reactive.sat.authentication.SatReactiveAuthenticationProvider.CLAIM_TENANT_ID;
import static org.talend.daikon.spring.reactive.sat.authentication.SatReactiveAuthenticationProvider.HEADER_SA_NAME;
import static org.talend.daikon.spring.reactive.sat.authentication.SatReactiveAuthenticationProvider.HEADER_TENANT_ID;

import java.util.Collection;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.talend.daikon.spring.auth.common.model.userdetails.AuthUserDetails;

public class SatReactiveAuthenticationToken extends ReactiveAuthenticationToken {

    public static final String USER_DETAILS_NAME_SA_SUFFIX = " - Service Account";

    public SatReactiveAuthenticationToken(Jwt decodedJwt, ServerHttpRequest request) {
        super(decodedJwt, request);
    }

    private static AuthUserDetails userDetails(String clientId, Collection<GrantedAuthority> authorities, String tenantId,
            String saName) {
        AuthUserDetails authUserDetails = new AuthUserDetails(saName + USER_DETAILS_NAME_SA_SUFFIX, "", authorities);
        authUserDetails.setTenantId(tenantId);
        authUserDetails.setTenantName(tenantId);
        authUserDetails.setId(clientId);
        return authUserDetails;
    }

    @Override
    protected AuthUserDetails computeUserDetails(String clientId, Collection<GrantedAuthority> authorities,
            ServerHttpRequest request) {
        String saName = request.getHeaders().get(HEADER_SA_NAME).get(0);
        String tenantId = request.getHeaders().get(HEADER_TENANT_ID).get(0);
        return userDetails(clientId, authorities, tenantId, saName);
    }

    @Override
    protected AuthUserDetails computeUserDetails(String clientId, Collection<GrantedAuthority> authorities, Jwt jwt) {
        String saName = getClaim(jwt, CLAIM_SA_NAME);
        String tenantId = getClaim(jwt, CLAIM_TENANT_ID);

        return userDetails(clientId, authorities, tenantId, saName);
    }

}
