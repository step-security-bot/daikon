package org.talend.daikon.spring.auth.introspection;

import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.talend.daikon.spring.auth.common.model.userdetails.AuthUserDetails;
import org.talend.daikon.spring.auth.common.model.userdetails.UserDetailsConverter;

/**
 * Returns {@link AuthUserDetails} object as a result of successful introspection
 */
public class AuthUserDetailsConverterIntrospector implements OpaqueTokenIntrospector {

    private final OpaqueTokenIntrospector delegate;

    public AuthUserDetailsConverterIntrospector(OpaqueTokenIntrospector delegate) {
        this.delegate = delegate;
    }

    public OAuth2AuthenticatedPrincipal introspect(String token) {
        return UserDetailsConverter.convert(delegate.introspect(token).getAttributes());
    }

}
