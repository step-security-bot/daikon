package org.talend.daikon.spring.license.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.talend.daikon.spring.license.client.client.LicenseClient;
import org.talend.daikon.spring.license.client.client.LicenseConfig;

import jakarta.servlet.ServletException;

public class KeepAliveLicenseFilterTest {

    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";

    private static final String ATTR_LAST_KEEP_ALIVE_TIMESTAMP = "KeepAliveLicenseFilter.lastKeepAliveTimestamp";

    private static final int KEEP_ALIVE_INTERVAL = 45;

    private static final long KEEP_ALIVE_INTERVAL_MS = KEEP_ALIVE_INTERVAL * 1000;

    private KeepAliveLicenseFilter filter;

    private LicenseClient licenseClientMock;

    private MockHttpServletRequest req = new MockHttpServletRequest();;

    private MockHttpServletResponse res = new MockHttpServletResponse();

    private MockFilterChain chain = new MockFilterChain();

    @BeforeEach
    public void setUp() {
        // newRequestContext();
        licenseClientMock = mock(LicenseClient.class);
        LicenseConfig config = new LicenseConfig();
        config.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
        when(licenseClientMock.getConfig()).thenReturn(config);
        filter = new KeepAliveLicenseFilter(licenseClientMock);

    }

    @Test
    public void givenOAuthRequestWhenFilterCalledShouldRunFilter() throws InterruptedException, ServletException, IOException {
        addAccessTokenToRequest("myAccessToken");

        assertTrue(filter.shouldFilter(req));
        verify(licenseClientMock, times(1)).getConfig();
        verify(licenseClientMock, times(0)).keepAlive();

        Date start = new Date();
        Thread.sleep(2L);
        filter.doFilter(req, res, chain);
        Thread.sleep(2L);
        Date end = new Date();
        verify(licenseClientMock, times(1)).keepAlive();
        Long lastKeepAlive = (Long) req.getSession().getAttribute(ATTR_LAST_KEEP_ALIVE_TIMESTAMP);
        assertNotNull(lastKeepAlive);
        assertTrue(lastKeepAlive >= start.getTime());
        assertTrue(lastKeepAlive <= end.getTime());
    }

    @Test
    public void givenNoOAuthRequestWhenFilterCalledShouldNotRunFilter() {
        assertFalse(filter.shouldFilter(req));
    }

    @Test
    public void givenKeepAliveWasCalledAfterLastIntervalWhenFilterCalledShouldNotRunFilter() {

        // last keepAlive was called recently, we should run the filter.
        Objects.requireNonNull(req.getSession()).setAttribute(ATTR_LAST_KEEP_ALIVE_TIMESTAMP,
                System.currentTimeMillis() - KEEP_ALIVE_INTERVAL_MS + 10000);
        addAccessTokenToRequest("myAccessToken");

        assertFalse(filter.shouldFilter(req));
        verify(licenseClientMock, times(1)).getConfig();
    }

    @Test
    public void givenKeepAliveWasCalledBeforeLastIntervalWhenFilterCalledShouldNotRunFilter() {

        // last keepAlive was called a long time ago...
        Objects.requireNonNull(req.getSession()).setAttribute(ATTR_LAST_KEEP_ALIVE_TIMESTAMP,
                System.currentTimeMillis() - KEEP_ALIVE_INTERVAL_MS - 1);
        addAccessTokenToRequest("myAccessToken");

        assertTrue(filter.shouldFilter(req));
        verify(licenseClientMock, times(1)).getConfig();
    }

    @Test
    public void givenGetConfigAlreadyCalledWhenFilterCalledShouldNotCallGetConfig() {
        addAccessTokenToRequest("myAccessToken");
        filter.shouldFilter(req);
        verify(licenseClientMock, times(1)).getConfig();
        reset(licenseClientMock);

        filter.shouldFilter(req);

        verify(licenseClientMock, times(0)).getConfig();
    }

    private void addAccessTokenToRequest(String accessToken) {
        req.setAttribute(ACCESS_TOKEN, accessToken);
        req.addHeader("authorization", accessToken);
    }

    // private void newRequestContext() {
    // newRequestContext(new MockHttpSession());
    // }
    //
    // private void newRequestContext(HttpSession session) {
    // RequestContext requestContext = new RequestContext();
    // MockHttpServletRequest servletRequest = new MockHttpServletRequest();
    // servletRequest.setSession(session);
    // requestContext.setRequest(servletRequest);
    // requestContext.setResponse(new MockHttpServletResponse());
    // RequestContext.testSetCurrentContext(requestContext);
    // }
}
