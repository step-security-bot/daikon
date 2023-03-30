package org.talend.daikon.spring.auth.multitenant;

import java.security.Principal;

import org.springframework.security.core.context.SecurityContextHolder;
import org.talend.daikon.multitenant.web.TenantIdentificationStrategy;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Retrieves the account name from the current Spring Security Context.
 *
 * This strategy just takes the principal.getName() and keeps the part after '@'
 * separator.
 *
 * @author agonzalez
 *
 * Extracted from oidc-client
 * https://github.com/Talend/platform-services-sdk/blob/222293d4d7decb2f1205e979ff6b08f202102226/oidc-client/src/main/java/org/talend/iam/security/account/AccountSecurityContextIdentificationStrategy.java
 *
 */
public class AccountSecurityContextIdentificationStrategy implements TenantIdentificationStrategy {

    /**
     * Get account name from running context if there's an account.
     */
    @Override
    public Object identifyTenant(HttpServletRequest request) {
        return identifyTenant();
    }

    public Object identifyTenant() {
        Principal principal = SecurityContextHolder.getContext().getAuthentication();
        if (!isAccountNameValid(principal)) {
            return null;
        }
        return accountName(principal);
    }

    /**
     * Checks that principal is not null and that principal.name verifies
     * user@accountName.
     */
    private boolean isAccountNameValid(Principal principal) {
        if (principal == null) {
            return false;
        }
        String name = principal.getName();
        if (name == null) {
            return false;
        }
        int separatorIndex = name.lastIndexOf('@');
        return separatorIndex > 0 && separatorIndex != name.length() - 1;
    }

    /**
     * Throws IllegalStateException is missing security context or accountName invalid.
     */
    private String accountName(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new IllegalStateException("No principal in securityContext");
        }
        int separatorIndex = principal.getName().lastIndexOf('@');
        if (separatorIndex <= 0 || separatorIndex == principal.getName().length() - 1) {
            String errorMessage = "Cannot get account from principal name %s, principal should end with @accountName";
            throw new IllegalStateException(String.format(errorMessage, principal.getName()));
        }
        return principal.getName().substring(separatorIndex + 1).toLowerCase();
    }
}
