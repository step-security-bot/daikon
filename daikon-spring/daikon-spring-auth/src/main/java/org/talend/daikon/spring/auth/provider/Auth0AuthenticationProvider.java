package org.talend.daikon.spring.auth.provider;

import java.util.Collections;
import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.talend.daikon.spring.auth.model.token.AuthenticationToken;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Provides info about mandatory claims/headers in case of authentication with different Auth0 JWT tokens
 * Builds AuthenticationToken with {@link org.talend.daikon.spring.auth.common.model.userdetails.AuthUserDetails} in case of
 * success
 */
public interface Auth0AuthenticationProvider {

    String HEADER_PERMISSIONS = "talend-permissions";

    String HEADER_CLIENT_ID = "talend-client-id";

    String CLAIM_PERMISSIONS = "https://talend.cloud/permissions";

    List<String> getMandatoryHeaders();

    List<String> getMandatoryClaims();

    AuthenticationToken buildAuthenticationToken(Jwt decodedJwt);

    default boolean isAlreadyAuthenticated() {
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            return Collections.list(request.getHeaderNames()).containsAll(getMandatoryHeaders());
        }
        return false;
    }

    default boolean mandatoryClaimsPresent(Jwt decodedJwt) {
        return decodedJwt.getClaims().keySet().containsAll(getMandatoryClaims());
    }
}
