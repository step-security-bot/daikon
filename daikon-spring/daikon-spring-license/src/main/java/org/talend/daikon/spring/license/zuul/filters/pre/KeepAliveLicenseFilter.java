package org.talend.daikon.spring.license.zuul.filters.pre;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

import org.talend.daikon.spring.license.client.client.LicenseClient;
import org.talend.daikon.spring.license.client.client.LicenseConfig;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public class KeepAliveLicenseFilter extends ZuulFilter {

    private static final String ATTR_LAST_KEEP_ALIVE_TIMESTAMP = "KeepAliveLicenseFilter.lastKeepAliveTimestamp";

    private static final int KEEP_ALIVE_FILTER_ORDER = 15;

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
    public String filterType() {
        return PRE_TYPE;
    }

    /**
     * Needs to be executed after OAuth2TokenRelayFilter
     */
    @Override
    public int filterOrder() {
        return KEEP_ALIVE_FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();

        // no need to call if REST Service is public and doesn't require accessToken
        // (aka if OAuth2TokenRelayFilter was not executed before)
        if (ctx.get(ACCESS_TOKEN) == null) {
            return false;
        }

        // no need to call if keepAlive endpoint has already been recently called
        Long timestamp = (Long) ctx.getRequest().getSession().getAttribute(ATTR_LAST_KEEP_ALIVE_TIMESTAMP);
        long interval = getLicenseConfig().getKeepAliveInterval() * 1000L;
        return timestamp == null || System.currentTimeMillis() > timestamp + interval;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        String bearerToken = ctx.getZuulRequestHeaders().get("authorization");
        if (bearerToken == null) {
            return null;
        }
        licenseClient.keepAlive();
        ctx.getRequest().getSession().setAttribute(ATTR_LAST_KEEP_ALIVE_TIMESTAMP, System.currentTimeMillis());
        return null;
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
