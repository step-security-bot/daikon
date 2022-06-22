package org.talend.daikon.spring.auth.model.token;

import static org.talend.daikon.spring.auth.provider.Auth0AuthenticationProvider.CLAIM_PERMISSIONS;
import static org.talend.daikon.spring.auth.provider.Auth0AuthenticationProvider.HEADER_CLIENT_ID;
import static org.talend.daikon.spring.auth.provider.Auth0AuthenticationProvider.HEADER_PERMISSIONS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.talend.daikon.spring.auth.common.model.userdetails.AuthUserDetails;

public abstract class AuthenticationToken implements Authentication {

    private AuthUserDetails authUserDetails;

    private Jwt decodedJwt;

    private Collection<GrantedAuthority> authorities = new ArrayList<>();

    public AuthenticationToken(Jwt decodedJwt) {
        this.decodedJwt = decodedJwt;
        computePrincipal();
    }

    private void computePrincipal() {
        if (this.decodedJwt != null) {
            computePrincipalFromJwt();
        } else {
            computePrincipalFromHttpRequest();
        }
    }

    private void computePrincipalFromJwt() {
        if (decodedJwt.containsClaim(CLAIM_PERMISSIONS)) {
            authorities = toAuthorities(decodedJwt.getClaimAsStringList(CLAIM_PERMISSIONS).stream());
        }

        String clientId = decodedJwt.getSubject().split("@")[0];

        this.authUserDetails = computeUserDetails(clientId, authorities, decodedJwt);
    }

    private void computePrincipalFromHttpRequest() {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

            if (request.getHeader(HEADER_PERMISSIONS) != null) {
                String[] permissions = request.getHeader(HEADER_PERMISSIONS).split(",");
                authorities = toAuthorities(Arrays.stream(permissions));
            }

            String clientId = request.getHeader(HEADER_CLIENT_ID);

            this.authUserDetails = computeUserDetails(clientId, authorities, request);
        } else {
            throw new IllegalStateException("Cannot compute Principal from http request: requestAttributes empty");
        }
    }

    protected abstract AuthUserDetails computeUserDetails(String clientId, Collection<GrantedAuthority> authorities,
            HttpServletRequest request);

    protected abstract AuthUserDetails computeUserDetails(String clientId, Collection<GrantedAuthority> authorities,
            Jwt decodedJwt);

    @Override
    public String getName() {
        if (this.authUserDetails.getTenantId() != null) {
            return this.authUserDetails.getUsername() + "@" + this.authUserDetails.getTenantId();
        } else {
            return this.authUserDetails.getUsername();
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return this.decodedJwt;
    }

    @Override
    public Object getDetails() {
        return this.decodedJwt;
    }

    @Override
    public Object getPrincipal() {
        return this.authUserDetails;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    }

    public static String getClaim(Jwt decodedJwt, String claimTenantId) {
        return decodedJwt.containsClaim(claimTenantId) ? decodedJwt.getClaimAsString(claimTenantId) : null;
    }

    private static Set<GrantedAuthority> toAuthorities(Stream<String> permissions) {
        return permissions.map(String::trim).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" [");
        sb.append("Principal=").append(getPrincipal()).append(", ");
        sb.append("Credentials=[PROTECTED], ");
        sb.append("Tenant ID=").append(this.authUserDetails.getTenantId()).append(", ");
        sb.append("Authenticated=").append(isAuthenticated()).append(", ");
        sb.append("Granted Authorities=").append(this.authorities);
        sb.append("]");
        return sb.toString();
    }

}
