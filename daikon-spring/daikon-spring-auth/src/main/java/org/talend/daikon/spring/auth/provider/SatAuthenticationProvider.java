package org.talend.daikon.spring.auth.provider;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;
import org.talend.daikon.spring.auth.model.token.AuthenticationToken;
import org.talend.daikon.spring.auth.model.token.SatAuthenticationToken;

/**
 * Authentication Provider for Service Accounts authentication handling
 */
public class SatAuthenticationProvider implements Auth0AuthenticationProvider {

    public static final String HEADER_TENANT_ID = "talend-tenant-id";

    public static final String HEADER_TENANT_NAME = "talend-tenant-name";

    public static final String HEADER_SA_NAME = "talend-service-account-name";

    public static final String CLAIM_TENANT_ID = "https://talend.cloud/tenantId";

    public static final String CLAIM_TENANT_NAME = "https://talend.cloud/tenantName";

    public static final String CLAIM_SA_NAME = "https://talend.cloud/serviceAccountName";

    private static final List<String> MANDATORY_SAT_HEADERS_LIST = Arrays.asList(HEADER_PERMISSIONS, HEADER_TENANT_ID,
            HEADER_CLIENT_ID, HEADER_SA_NAME);

    private static final List<String> MANDATORY_SAT_CLAIMS_LIST = Arrays.asList(CLAIM_PERMISSIONS, CLAIM_TENANT_ID,
            CLAIM_SA_NAME);

    @Override
    public List<String> getMandatoryHeaders() {
        return MANDATORY_SAT_HEADERS_LIST;
    }

    @Override
    public List<String> getMandatoryClaims() {
        return MANDATORY_SAT_CLAIMS_LIST;
    }

    @Override
    public AuthenticationToken buildAuthenticationToken(Jwt decodedJwt) {
        return new SatAuthenticationToken(decodedJwt);
    }
}
