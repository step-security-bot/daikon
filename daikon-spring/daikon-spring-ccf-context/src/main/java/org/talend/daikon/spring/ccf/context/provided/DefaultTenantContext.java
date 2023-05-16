package org.talend.daikon.spring.ccf.context.provided;

import org.talend.iam.scim.model.User;

import lombok.Builder;
import lombok.Data;

/**
 * Class to represent the context outside of security context.
 * Enrich it if needed.
 */
@Data
@Builder
public class DefaultTenantContext {

    private String tenantId;

    private String userId;

    private User currentUser;
}
