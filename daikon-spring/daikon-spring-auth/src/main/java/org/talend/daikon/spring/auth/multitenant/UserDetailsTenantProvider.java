package org.talend.daikon.spring.auth.multitenant;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.talend.daikon.multitenant.core.Tenant;
import org.talend.daikon.multitenant.provider.DefaultTenant;
import org.talend.daikon.multitenant.provider.TenantProvider;
import org.talend.daikon.spring.auth.common.model.userdetails.AuthUserDetails;

/**
 * Tenant provider that reads the information about a Tenant from the UserDetails object,
 * which was previously populated with data from the JWT claims.
 *
 * Extracted from oidc-client
 * https://github.com/Talend/platform-services-sdk/blob/3782f844c7a65096eb340971b6b8d7da2d0fc118/oidc-client/src/main/java/org/talend/iam/security/account/UserDetailsTenantProvider.java
 */
public class UserDetailsTenantProvider implements TenantProvider {

    @Override
    public Tenant findTenant(Object identity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthUserDetails user = (AuthUserDetails) authentication.getPrincipal();
        if (user.getTenantName() != null && identity.toString().equals(user.getTenantName())) {
            String tenantId = user.getTenantId();
            if (tenantId != null) {
                return new DefaultTenant(tenantId, user.getTenantName());
            }
        }

        return null;
    }

}
