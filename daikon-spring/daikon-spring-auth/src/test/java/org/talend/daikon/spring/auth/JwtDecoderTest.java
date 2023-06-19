package org.talend.daikon.spring.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;

public class JwtDecoderTest {

    private static final String AUTHZ_TOKEN = "<AUTHZ_TOKEN>";

    private static final String IDP_TOKEN = "<IDP_TOKEN>";

    @Test
    public void testAuthzTokenWithIdpJwk() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri("https://iam.at.cloud.talend.com/oidc/jwk/keys").build();
        jwtDecoder.decode(AUTHZ_TOKEN);

        NimbusReactiveJwtDecoder reactiveJwtDecoder = NimbusReactiveJwtDecoder
                .withJwkSetUri("https://iam.at.cloud.talend.com/oidc/jwk/keys").build();
        reactiveJwtDecoder.decode(AUTHZ_TOKEN).block();
    }

    @Test
    public void testIdpTokenWithIdpJwk() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri("https://iam.at.cloud.talend.com/oidc/jwk/keys").build();
        jwtDecoder.decode(IDP_TOKEN);

        NimbusReactiveJwtDecoder reactiveJwtDecoder = NimbusReactiveJwtDecoder
                .withJwkSetUri("https://iam.at.cloud.talend.com/oidc/jwk/keys").build();
        reactiveJwtDecoder.decode(IDP_TOKEN).block();
    }

    @Test
    public void testAuthzTokenWithAuthzJwk() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri("https://iam.int.cloud.talend.com/oidc/jwk/keys").build();
        jwtDecoder.decode(AUTHZ_TOKEN);

        NimbusReactiveJwtDecoder reactiveJwtDecoder = NimbusReactiveJwtDecoder
                .withJwkSetUri("https://iam.int.cloud.talend.com/oidc/jwk/keys").build();
        reactiveJwtDecoder.decode(AUTHZ_TOKEN).block();
    }

    @Test
    public void testIdpTokenWithAuthzJwk() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri("https://iam.int.cloud.talend.com/oidc/jwk/keys").build();
        jwtDecoder.decode(IDP_TOKEN);

        NimbusReactiveJwtDecoder reactiveJwtDecoder = NimbusReactiveJwtDecoder
                .withJwkSetUri("https://iam.int.cloud.talend.com/oidc/jwk/keys").build();
        reactiveJwtDecoder.decode(IDP_TOKEN).block();
    }
}
