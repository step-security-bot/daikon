package org.talend.daikon.spring.auth.model.token;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.talend.daikon.spring.auth.model.token.SatAuthenticationToken.USER_DETAILS_NAME_SA_SUFFIX;
import static org.talend.daikon.spring.auth.provider.Auth0AuthenticationProvider.CLAIM_PERMISSIONS;
import static org.talend.daikon.spring.auth.provider.Auth0AuthenticationProvider.HEADER_CLIENT_ID;
import static org.talend.daikon.spring.auth.provider.Auth0AuthenticationProvider.HEADER_PERMISSIONS;
import static org.talend.daikon.spring.auth.provider.SatAuthenticationProvider.*;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.talend.daikon.spring.auth.common.model.userdetails.AuthUserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
public class SatAuthenticationTokenTest {

    private static final String SUBJECT = "aocZUQ8wJWdMGjM2HpfCUvw0NlplMf9k@clients";

    private static final String CLIENT_ID = "aocZUQ8wJWdMGjM2HpfCUvw0NlplMf9k";

    private static final String SA_NAME = "My awesome service account";

    private static final String TENANT_ID = "ce8bca4e-c6c2-42b5-8705-02b8af3a94bb";

    private static final String TENANT_NAME = "tenant.name";

    private static final Collection<String> PERMISSIONS = Arrays.asList("TMC_USER_MANAGEMENT", "TMC_GROUP_MANAGEMENT",
            "TMC_ROLE_MANAGEMENT");

    private MockHttpServletRequest request;

    @BeforeEach
    public void setup() {
        this.request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    public void extractClaimsFromHttpHeadersByDefault() {
        request.addHeader(HEADER_TENANT_ID, "01619a4d-955f-4ca3-8813-aa66bfee83a0");
        request.addHeader(HEADER_PERMISSIONS, "ANOTHER_PERMISSION_1,ANOTHER_PERMISSION_2");
        request.addHeader(HEADER_CLIENT_ID, "another_subject");
        request.addHeader(HEADER_SA_NAME, "Another service account");

        Authentication authentication = new SatAuthenticationToken(null);

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
        Authentication authentication = new SatAuthenticationToken(jwt());

        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        AuthUserDetails user = (AuthUserDetails) authentication.getPrincipal();

        assertThat(authorities, containsInAnyOrder(PERMISSIONS.toArray()));
        assertThat(user.getTenantId(), is(TENANT_ID));
        assertThat(user.getId(), is(CLIENT_ID));
        assertThat(user.getUsername(), is(SA_NAME + USER_DETAILS_NAME_SA_SUFFIX));
    }

    @Test
    public void extractClaimsFromHttpHeadersWithTenantName() {
        request.addHeader(HEADER_TENANT_ID, "01619a4d-955f-4ca3-8813-aa66bfee83a0");
        request.addHeader(HEADER_TENANT_NAME, "tenant.name");
        request.addHeader(HEADER_PERMISSIONS, "ANOTHER_PERMISSION_1,ANOTHER_PERMISSION_2");
        request.addHeader(HEADER_CLIENT_ID, "another_subject");
        request.addHeader(HEADER_SA_NAME, "Another service account");

        Authentication authentication = new SatAuthenticationToken(null);

        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        AuthUserDetails auth = (AuthUserDetails) authentication.getPrincipal();

        assertThat(authorities, containsInAnyOrder("ANOTHER_PERMISSION_1", "ANOTHER_PERMISSION_2"));
        assertThat(auth.getTenantId(), is("01619a4d-955f-4ca3-8813-aa66bfee83a0"));
        assertThat(auth.getTenantName(), is("tenant.name"));
        assertThat(auth.getId(), is("another_subject"));
        assertThat(auth.getUsername(), is("Another service account - Service Account"));
        assertThat(authentication.getName(), is("Another service account - Service Account@" + TENANT_NAME));
    }

    @Test
    public void extractClaimsFromJwtWithTenantName() {
        Authentication authentication = new SatAuthenticationToken(jwtWithTenantName());

        List<String> authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        AuthUserDetails auth = (AuthUserDetails) authentication.getPrincipal();

        assertThat(authorities, containsInAnyOrder(PERMISSIONS.toArray()));
        assertThat(auth.getTenantId(), is(TENANT_ID));
        assertThat(auth.getTenantName(), is(TENANT_NAME));
        assertThat(auth.getId(), is(CLIENT_ID));
        assertThat(auth.getUsername(), is(SA_NAME + USER_DETAILS_NAME_SA_SUFFIX));
        assertThat(authentication.getName(), is(SA_NAME + USER_DETAILS_NAME_SA_SUFFIX + "@" + TENANT_NAME));
    }

    private Jwt jwt() {
        // @formatter:off
        JWT jwt = new PlainJWT(new JWTClaimsSet.Builder().build());

        return Jwt.withTokenValue(jwt.serialize())
                .claim(CLAIM_PERMISSIONS, PERMISSIONS)
                .claim(CLAIM_TENANT_ID, TENANT_ID)
                .claim(CLAIM_SA_NAME, SA_NAME)
                .subject(SUBJECT)
                .header("typ", "JWT").build();
        // @formatter:on
    }

    private Jwt jwtWithTenantName() {
        // @formatter:off
        JWT jwt = new PlainJWT(new JWTClaimsSet.Builder().build());

        return Jwt.withTokenValue(jwt.serialize())
                .claim(CLAIM_PERMISSIONS, PERMISSIONS)
                .claim(CLAIM_TENANT_ID, TENANT_ID)
                .claim(CLAIM_TENANT_NAME, TENANT_NAME)
                .claim(CLAIM_SA_NAME, SA_NAME)
                .subject(SUBJECT)
                .header("typ", "JWT").build();
        // @formatter:on
    }

}
