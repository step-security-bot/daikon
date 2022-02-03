package org.talend.daikon.spring.reactive.sat.authentication;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.talend.daikon.spring.reactive.sat.model.token.ReactiveAuthenticationToken;
import org.talend.daikon.spring.reactive.sat.model.token.SatReactiveAuthenticationToken;

/**
 * Authentication Provider for Service Accounts authentication handling
 */
public class SatReactiveAuthenticationProvider implements Auth0ReactiveAuthenticationProvider {

    public static final String HEADER_TENANT_ID = "talend-tenant-id";

    public static final String HEADER_SA_NAME = "talend-service-account-name";

    public static final String CLAIM_TENANT_ID = "https://talend.cloud/tenantId";

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
    public ReactiveAuthenticationToken buildAuthenticationToken(Jwt decodedJwt, ServerHttpRequest request) {
        return new SatReactiveAuthenticationToken(decodedJwt, request);
    }
}
