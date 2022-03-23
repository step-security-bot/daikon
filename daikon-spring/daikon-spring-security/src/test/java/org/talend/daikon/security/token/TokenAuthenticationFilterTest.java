package org.talend.daikon.security.token;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TokenAuthenticationFilterTest {

    private final ThreadLocal<Authentication> capturedAuthentication = new ThreadLocal<>();

    private final TokenAuthenticationFilter filter;

    private final HttpServletRequest request;

    private final FilterChain filterChain;

    private final HttpServletResponse response;

    public TokenAuthenticationFilterTest() throws IOException, ServletException {
        filterChain = mock(FilterChain.class);
        doAnswer(invocationOnMock -> {
            capturedAuthentication.set(SecurityContextHolder.getContext().getAuthentication());
            return null;
        }).when(filterChain).doFilter(any(), any());
        response = mock(HttpServletResponse.class);
        request = mock(HttpServletRequest.class);
        when(request.getServletPath()).thenReturn("/actuator/prometheus");

        final RequestMatcher requestMatcher = new AntPathRequestMatcher("/actuator/**");
        filter = new TokenAuthenticationFilter("myToken", requestMatcher);
    }

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();
        capturedAuthentication.remove();

        reset(request, response);
    }

    @Test
    public void shouldRejectRequestWithNoHeader() throws IOException, ServletException {
        // given
        when(request.getServletPath()).thenReturn("/actuator/prometheus");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertNull(capturedAuthentication.get());
    }

    @Test
    public void shouldAllowRequestWithValidToken() throws IOException, ServletException {
        // given
        when(request.getServletPath()).thenReturn("/actuator/prometheus");
        when(request.getHeader(eq("Authorization"))).thenReturn("Talend myToken");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertNotNull(capturedAuthentication.get());
        final Authentication authentication = capturedAuthentication.get();
        assertTrue(authentication.getAuthorities().stream().allMatch(g -> {
            final String authority = g.getAuthority();
            return "ROLE_ACTUATOR".equals(authority);
        }));
    }

    @Test
    public void shouldRejectRequestWithInvalidTokenFormat() throws IOException, ServletException {
        // given
        when(request.getServletPath()).thenReturn("/actuator/prometheus");
        when(request.getHeader(eq("Authorization"))).thenReturn("xxx");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertNull(capturedAuthentication.get());
    }

    @Test
    public void shouldRejectRequestWithInvalidTokenValue() throws IOException, ServletException {
        // given
        when(request.getServletPath()).thenReturn("/actuator/prometheus");
        when(request.getHeader(eq("Authorization"))).thenReturn("Talend xxx");

        // when
        filter.doFilter(request, response, filterChain);

        // then
        assertNull(capturedAuthentication.get());
    }

}