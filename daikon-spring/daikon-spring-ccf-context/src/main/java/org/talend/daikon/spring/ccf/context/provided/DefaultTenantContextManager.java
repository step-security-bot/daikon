package org.talend.daikon.spring.ccf.context.provided;

import java.util.Optional;

import org.talend.daikon.spring.ccf.context.M2MContextManager;
import org.talend.iam.scim.model.User;

public class DefaultTenantContextManager implements M2MContextManager {

    @Override
    public void clearContext() {
        DefaultTenantContextHolder.clear();
    }

    @Override
    public void injectContext(String tenantId, String userId, Optional<User> user) {
        DefaultTenantContextHolder.setContextWithTenantId(tenantId);
        DefaultTenantContextHolder.getContext().setUserId(userId);
        user.ifPresent(DefaultTenantContextHolder.getContext()::setCurrentUser);
    }
}
