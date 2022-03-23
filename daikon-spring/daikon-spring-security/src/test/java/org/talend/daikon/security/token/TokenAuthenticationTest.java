package org.talend.daikon.security.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

public class TokenAuthenticationTest {

    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        authentication = TokenAuthentication.INSTANCE;
    }

    @Test
    public void shouldHaveExpectedInformation() {
        assertTrue(authentication.isAuthenticated());
        assertEquals(TokenAuthentication.ADMIN_TOKEN_AUTHENTICATION, authentication.getDetails());
        assertNull(authentication.getCredentials());
        assertEquals(TokenAuthentication.ADMIN_TOKEN_AUTHENTICATION, authentication.getPrincipal());
    }

    @Test
    public void shouldNotAllowAuthenticatedChange() {
        assertThrows(IllegalArgumentException.class, () -> {
            authentication.setAuthenticated(false);
        });
    }

    @Test
    public void shouldAllowAuthenticatedEnable() {
        // Where setAuthenticated(false) is *not* possible, setAuthenticated(true) is still allowed.
        authentication.setAuthenticated(true);
    }
}
