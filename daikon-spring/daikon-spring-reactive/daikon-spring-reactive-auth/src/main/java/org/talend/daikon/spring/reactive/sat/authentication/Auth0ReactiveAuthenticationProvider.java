package org.talend.daikon.spring.reactive.sat.authentication;

import java.util.List;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.talend.daikon.spring.auth.common.model.userdetails.AuthUserDetails;
import org.talend.daikon.spring.reactive.sat.model.token.ReactiveAuthenticationToken;

/**
 * Provides info about mandatory claims/headers in case of authentication with different Auth0 JWT tokens
 * Builds AuthenticationToken with {@link AuthUserDetails} in case of success
 */
public interface Auth0ReactiveAuthenticationProvider {

    String HEADER_PERMISSIONS = "talend-permissions";

    String HEADER_CLIENT_ID = "talend-client-id";

    String CLAIM_PERMISSIONS = "https://talend.cloud/permissions";

    List<String> getMandatoryHeaders();

    List<String> getMandatoryClaims();

    ReactiveAuthenticationToken buildAuthenticationToken(Jwt decodedJwt, ServerHttpRequest request);

    default boolean isAlreadyAuthenticated(ServerHttpRequest request) {
        return request.getHeaders().keySet().containsAll(getMandatoryHeaders());
    }

    default boolean mandatoryClaimsPresent(Jwt decodedJwt) {
        return decodedJwt.getClaims().keySet().containsAll(getMandatoryClaims());
    }
}
