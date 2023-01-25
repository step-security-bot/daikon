package org.talend.daikon.spring.auth.model.token;

import static org.talend.daikon.spring.auth.provider.SatAuthenticationProvider.*;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.talend.daikon.spring.auth.common.model.userdetails.AuthUserDetails;

public class SatAuthenticationToken extends AuthenticationToken {

    public static final String USER_DETAILS_NAME_SA_SUFFIX = " - Service Account";

    public SatAuthenticationToken(Jwt decodedJwt) {
        super(decodedJwt);
    }

    @Override
    protected AuthUserDetails computeUserDetails(String clientId, Collection<GrantedAuthority> authorities,
            HttpServletRequest request) {
        String saName = request.getHeader(HEADER_SA_NAME);
        String tenantId = request.getHeader(HEADER_TENANT_ID);
        String tenantName = request.getHeader(HEADER_TENANT_NAME);

        return userDetails(clientId, authorities, tenantId, tenantName, saName);
    }

    @Override
    protected AuthUserDetails computeUserDetails(String clientId, Collection<GrantedAuthority> authorities, Jwt jwt) {
        String saName = getClaim(jwt, CLAIM_SA_NAME);
        String tenantId = getClaim(jwt, CLAIM_TENANT_ID);
        String tenantName = getClaim(jwt, CLAIM_TENANT_NAME);

        return userDetails(clientId, authorities, tenantId, tenantName, saName);
    }

    private static AuthUserDetails userDetails(String clientId, Collection<GrantedAuthority> authorities, String tenantId,
            String tenantName, String saName) {
        AuthUserDetails authUserDetails = new AuthUserDetails(saName + USER_DETAILS_NAME_SA_SUFFIX, "", authorities);
        authUserDetails.setTenantId(tenantId);
        authUserDetails.setTenantName(tenantName != null ? tenantName : tenantId);
        authUserDetails.setId(clientId);
        return authUserDetails;
    }

}
