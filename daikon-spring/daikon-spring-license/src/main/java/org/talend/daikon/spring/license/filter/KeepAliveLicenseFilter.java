package org.talend.daikon.spring.license.filter;

import java.io.IOException;

import org.talend.daikon.spring.license.client.client.LicenseClient;
import org.talend.daikon.spring.license.client.client.LicenseConfig;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

public class KeepAliveLicenseFilter implements Filter {

    private static final String ATTR_LAST_KEEP_ALIVE_TIMESTAMP = "KeepAliveLicenseFilter.lastKeepAliveTimestamp";

    private static final String ACCESS_TOKEN = "ACCESS_TOKEN";

    private LicenseClient licenseClient;

    private volatile LicenseConfig licenseConfig;

    public KeepAliveLicenseFilter(LicenseClient licenseClient) {
        if (licenseClient == null) {
            throw new IllegalArgumentException("licenseClient argument is required");
        }
        this.licenseClient = licenseClient;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws ServletException, IOException {
        if (shouldFilter(servletRequest)) {
            HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
            String bearerToken = httpRequest.getHeader("Authorization");
            if (bearerToken != null) {
                licenseClient.keepAlive();
                httpRequest.getSession().setAttribute(ATTR_LAST_KEEP_ALIVE_TIMESTAMP, System.currentTimeMillis());
            }
        }
        filterChain.doFilter(servletRequest, servletResponse);

    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    public boolean shouldFilter(ServletRequest servletRequest) {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

        // no need to call if REST Service is public and doesn't require accessToken
        // (aka if OAuth2TokenRelayFilter was not executed before)
        if (httpRequest.getAttribute(ACCESS_TOKEN) == null) {
            return false;
        }

        // no need to call if keepAlive endpoint has already been recently called
        Long timestamp = (Long) httpRequest.getSession().getAttribute(ATTR_LAST_KEEP_ALIVE_TIMESTAMP);
        long interval = getLicenseConfig().getKeepAliveInterval() * 1000L;
        return timestamp == null || System.currentTimeMillis() > timestamp + interval;
    }

    private LicenseConfig getLicenseConfig() {
        LicenseConfig result = licenseConfig;
        if (result == null) {
            synchronized (this) {
                result = licenseConfig;
                if (result == null) {
                    result = licenseClient.getConfig();
                    licenseConfig = result;
                }
            }
        }
        return result;
    }

}
