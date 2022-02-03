package org.talend.daikon.spring.reactive.sat.model.token;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.talend.daikon.spring.reactive.sat.authentication.Auth0ReactiveAuthenticationProvider.*;
import static org.talend.daikon.spring.reactive.sat.authentication.SatReactiveAuthenticationProvider.*;
import static org.talend.daikon.spring.reactive.sat.model.token.SatReactiveAuthenticationToken.USER_DETAILS_NAME_SA_SUFFIX;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.talend.daikon.spring.auth.common.model.userdetails.AuthUserDetails;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;

public class SatReactiveAuthenticationTokenTest {

    private static final String SUBJECT = "aocZUQ8wJWdMGjM2HpfCUvw0NlplMf9k@clients";

    private static final String CLIENT_ID = "aocZUQ8wJWdMGjM2HpfCUvw0NlplMf9k";

    private static final String SA_NAME = "My awesome service account";

    private static final String TENANT_ID = "ce8bca4e-c6c2-42b5-8705-02b8af3a94bb";

    private static final Collection<String> PERMISSIONS = Arrays.asList("TMC_USER_MANAGEMENT", "TMC_GROUP_MANAGEMENT",
            "TMC_ROLE_MANAGEMENT");

    private MockServerHttpRequest request;

    @Before
    public void setup() {
        this.request = MockServerHttpRequest.get("/").build();
    }

    @Test
    public void extractClaimsFromHttpHeadersByDefault() {
        Consumer<HttpHeaders> httpHeaders = httpHeader -> {
            httpHeader.add(HEADER_TENANT_ID, "01619a4d-955f-4ca3-8813-aa66bfee83a0");
            httpHeader.addAll(HEADER_PERMISSIONS, Arrays.asList("ANOTHER_PERMISSION_1", "ANOTHER_PERMISSION_2"));
            httpHeader.add(HEADER_CLIENT_ID, "another_subject");
            httpHeader.add(HEADER_SA_NAME, "Another service account");
        };
        request.mutate().headers(httpHeaders);
        Authentication authentication = new SatReactiveAuthenticationToken(null, request);

        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        AuthUserDetails user = (AuthUserDetails) authentication.getPrincipal();

        assertThat(authorities, containsInAnyOrder("ANOTHER_PERMISSION_1", "ANOTHER_PERMISSION_2"));
        assertThat(user.getTenantId(), is("01619a4d-955f-4ca3-8813-aa66bfee83a0"));
        assertThat(user.getId(), is("another_subject"));
        assertThat(user.getUsername(), is("Another service account - Service Account"));
    }

    @Test
    public void extractClaimsFromJwtIfNoHttpHeaders() {
        Authentication authentication = new SatReactiveAuthenticationToken(jwt(), request);

        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        AuthUserDetails user = (AuthUserDetails) authentication.getPrincipal();

        assertThat(authorities, containsInAnyOrder(PERMISSIONS.toArray()));
        assertThat(user.getTenantId(), is(TENANT_ID));
        assertThat(user.getId(), is(CLIENT_ID));
        assertThat(user.getUsername(), is(SA_NAME + USER_DETAILS_NAME_SA_SUFFIX));
    }

    private Jwt jwt() {
        JWT jwt = new PlainJWT(new JWTClaimsSet.Builder().claim(CLAIM_PERMISSIONS, PERMISSIONS).claim(CLAIM_TENANT_ID, TENANT_ID)
                .claim(CLAIM_SA_NAME, SA_NAME).subject(SUBJECT).build());

        return Jwt.withTokenValue(jwt.serialize()).claim(CLAIM_PERMISSIONS, PERMISSIONS).claim(CLAIM_TENANT_ID, TENANT_ID)
                .claim(CLAIM_SA_NAME, SA_NAME).subject(SUBJECT).header("typ", "JWT").build();
    }

}
