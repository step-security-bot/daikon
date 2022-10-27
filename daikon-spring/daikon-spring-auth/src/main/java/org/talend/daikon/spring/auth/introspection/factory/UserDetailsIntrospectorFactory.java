package org.talend.daikon.spring.auth.introspection.factory;

import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

public interface UserDetailsIntrospectorFactory {

    OpaqueTokenIntrospector build(OpaqueTokenIntrospector delegate);

}
