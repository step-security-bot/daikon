package org.talend.daikon.spring.auth.introspection.factory;

import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.talend.daikon.spring.auth.introspection.AuthUserDetailsConverterIntrospectorOnPrem;
import org.talend.iam.im.scim.client.MeClient;

public class UserDetailsIntrospectorFactoryOnPrem implements UserDetailsIntrospectorFactory {

    private MeClient meClient;

    public UserDetailsIntrospectorFactoryOnPrem(MeClient meClient) {
        this.meClient = meClient;
    }

    public OpaqueTokenIntrospector build(OpaqueTokenIntrospector delegate) {
        AuthUserDetailsConverterIntrospectorOnPrem introspector = new AuthUserDetailsConverterIntrospectorOnPrem(delegate);
        introspector.setMeClient(meClient);
        return introspector;
    }

}
