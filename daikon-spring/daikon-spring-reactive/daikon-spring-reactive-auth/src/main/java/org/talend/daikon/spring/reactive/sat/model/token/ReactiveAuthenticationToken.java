package org.talend.daikon.spring.reactive.sat.model.token;

import static org.talend.daikon.spring.reactive.sat.authentication.Auth0ReactiveAuthenticationProvider.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.CollectionUtils;
import org.talend.daikon.spring.auth.common.model.userdetails.AuthUserDetails;

public abstract class ReactiveAuthenticationToken implements Authentication {

    private AuthUserDetails authUserDetails;

    private final Jwt decodedJwt;

    private Collection<GrantedAuthority> authorities = new ArrayList<>();

    ReactiveAuthenticationToken(Jwt decodedJwt, ServerHttpRequest request) {
        this.decodedJwt = decodedJwt;
        computePrincipal(request);
    }

    public static String getClaim(Jwt decodedJwt, String claimTenantId) {
        return decodedJwt.containsClaim(claimTenantId) ? decodedJwt.getClaimAsString(claimTenantId) : null;
    }

    private static Set<GrantedAuthority> toAuthorities(List<String> permissions) {
        return permissions.stream().map(String::trim).map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    private void computePrincipal(ServerHttpRequest request) {
        if (this.decodedJwt != null) {
            computePrincipalFromJwt();
        } else {
            computePrincipalFromHttpRequest(request);
        }
    }

    private void computePrincipalFromJwt() {
        if (decodedJwt.containsClaim(CLAIM_PERMISSIONS)) {
            authorities = toAuthorities(decodedJwt.getClaimAsStringList(CLAIM_PERMISSIONS));
        }
        String clientId = decodedJwt.getSubject().split("@")[0];
        this.authUserDetails = computeUserDetails(clientId, authorities, decodedJwt);
    }

    private void computePrincipalFromHttpRequest(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        if (!CollectionUtils.isEmpty(headers)) {
            if (!CollectionUtils.isEmpty(headers.get(HEADER_PERMISSIONS))) {
                authorities = toAuthorities(headers.get(HEADER_PERMISSIONS));
            }
            if (!CollectionUtils.isEmpty(headers.get(HEADER_CLIENT_ID))) {
                String clientId = headers.get(HEADER_CLIENT_ID).get(0);
                this.authUserDetails = computeUserDetails(clientId, authorities, request);
            }
        } else {
            throw new IllegalStateException("Cannot compute Principal from http request: no headers");
        }
    }

    protected abstract AuthUserDetails computeUserDetails(String clientId, Collection<GrantedAuthority> authorities,
            ServerHttpRequest request);

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

}
