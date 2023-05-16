package org.talend.daikon.spring.ccf.context;

import java.util.Optional;

import org.talend.iam.scim.model.User;

public interface M2MContextManager {

    void clearContext();

    void injectContext(String tenantId, String userId, Optional<User> user);
}
