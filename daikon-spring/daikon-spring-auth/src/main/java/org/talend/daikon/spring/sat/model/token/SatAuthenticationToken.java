package org.talend.daikon.spring.sat.model.token;

import static org.talend.daikon.spring.sat.provider.SatAuthenticationProvider.CLAIM_SA_NAME;
import static org.talend.daikon.spring.sat.provider.SatAuthenticationProvider.CLAIM_TENANT_ID;
import static org.talend.daikon.spring.sat.provider.SatAuthenticationProvider.HEADER_SA_NAME;
import static org.talend.daikon.spring.sat.provider.SatAuthenticationProvider.HEADER_TENANT_ID;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.talend.daikon.spring.sat.model.userdetails.AuthUserDetails;

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

        return userDetails(clientId, authorities, tenantId, saName);
    }

    @Override
    protected AuthUserDetails computeUserDetails(String clientId, Collection<GrantedAuthority> authorities, Jwt jwt) {
        String saName = getClaim(jwt, CLAIM_SA_NAME);
        String tenantId = getClaim(jwt, CLAIM_TENANT_ID);

        return userDetails(clientId, authorities, tenantId, saName);
    }

    private static AuthUserDetails userDetails(String clientId, Collection<GrantedAuthority> authorities, String tenantId,
            String saName) {
        AuthUserDetails authUserDetails = new AuthUserDetails(saName + USER_DETAILS_NAME_SA_SUFFIX, "", authorities);
        authUserDetails.setTenantId(tenantId);
        authUserDetails.setTenantName(tenantId);
        authUserDetails.setId(clientId);
        return authUserDetails;
    }

}
