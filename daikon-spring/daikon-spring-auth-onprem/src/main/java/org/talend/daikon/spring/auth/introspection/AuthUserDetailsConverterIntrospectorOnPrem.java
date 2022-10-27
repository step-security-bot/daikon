package org.talend.daikon.spring.auth.introspection;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.talend.daikon.spring.auth.common.model.userdetails.AuthUserDetails;
import org.talend.daikon.spring.auth.common.model.userdetails.JwtClaims;
import org.talend.daikon.spring.auth.common.model.userdetails.UserDetailsConverter;
import org.talend.iam.im.scim.client.MeClient;
import org.talend.iam.scim.model.User;

/**
 * Returns {@link AuthUserDetails} object as a result of successful introspection
 */
public class AuthUserDetailsConverterIntrospectorOnPrem implements OpaqueTokenIntrospector {

    private final OpaqueTokenIntrospector delegate;

    private MeClient meClient;

    public AuthUserDetailsConverterIntrospectorOnPrem(OpaqueTokenIntrospector delegate) {
        this.delegate = delegate;
    }

    public void setMeClient(MeClient meClient) {
        this.meClient = meClient;
    }

    public OAuth2AuthenticatedPrincipal introspect(String token) {
        OAuth2AuthenticatedPrincipal principal = delegate.introspect(token);
        Map<String, Object> jwtClaims = new HashMap<>(principal.getAttributes());

        User user = meClient.getCurrentUser();
        String entitlementsList = user.getEntitlements().stream().map(e -> e.getValue()).collect(Collectors.joining(","));
        jwtClaims.put(JwtClaims.ENTITLEMENTS_CLAIM, entitlementsList);

        AuthUserDetails userDetails = UserDetailsConverter.convert(jwtClaims);
        userDetails.setId(user.getId());

        return userDetails;
    }
}
