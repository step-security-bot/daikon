package org.talend.daikon.spring.auth.common.model.userdetails;

/**
 * Talend IAM JWT claims as defined in oidc-client
 * https://github.com/Talend/platform-services-sdk/blob/8fac1268ae7f2af33d44ae6b49ff01a2738276e9/oidc-client/src/main/java/org/talend/iam/security/oidc/client/token/JwtClaims.java
 */
public class JwtClaims {

    public static final String ISSUER_CLAIM = "iss";

    public static final String SUBJECT_CLAIM = "sub";

    public static final String AUDIENCE_CLAIM = "aud";

    public static final String EXPIRATION_TIME_CLAIM = "exp";

    public static final String NOT_BEFORE_CLAIM = "nbf";

    public static final String ISSUED_AT_CLAIM = "iat";

    public static final String JWT_ID_CLAIM = "jti";

    public static final String NAME_CLAIM = "name";

    public static final String USERNAME_CLAIM = "username";

    public static final String EMAIL_CLAIM = "email";

    public static final String GIVEN_NAME_CLAIM = "given_name";

    public static final String MIDDLE_NAME_CLAIM = "middle_name";

    public static final String FAMILY_NAME_CLAIM = "family_name";

    public static final String PREFERRED_LANGUAGE_CLAIM = "preferred_language";

    public static final String TIMEZONE_CLAIM = "timezone";

    public static final String ENTITLEMENTS_CLAIM = "entitlements";

    public static final String APPLICATIONS_CLAIM = "applications";

    public static final String GROUP_IDS_CLAIM = "group_ids";

    public static final String TENANT_ID_CLAIM = "tenant_id";

    public static final String TENANT_NAME_CLAIM = "tenant_name";

    public static final String PENDO_USER_ID_CLAIM = "pendo_user_id";

    public static final String PENDO_COMPANY_NAME_CLAIM = "pendo_company_name";

    public static final String PENDO_DATACENTER_CLAIM = "pendo_datacenter";

    public static final String SUBSCRIPTION_TYPE = "subscription_type";

    public static final String SALES_FORCE_CONTACT_ID = "sales_force_contact_id";

    public static final String SALES_FORCE_ACCOUNT_ID = "sales_force_account_id";

    public static final String IPC_ENABLED = "ipc_enabled";

}
