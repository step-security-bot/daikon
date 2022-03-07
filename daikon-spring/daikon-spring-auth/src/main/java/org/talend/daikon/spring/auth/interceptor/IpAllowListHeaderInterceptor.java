package org.talend.daikon.spring.auth.interceptor;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Http request interceptor to be used with RestTemplate
 * Propagates X-Forwarded-For header to downstream services
 *
 * Extracted from oidc-client
 * https://github.com/Talend/platform-services-sdk/blob/10c55863f05bcb65808e66acf2adb13b5f5358c3/oidc-client/src/main/java/org/talend/iam/security/autoconfigure/oauth/client/TalendOAuth2RequestAuthenticator.java
 */
public class IpAllowListHeaderInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(IpAllowListHeaderInterceptor.class);

    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        // IP Allowlisting
        // Retrieve remoteAddress to pass in introspection header request
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest contextHttpRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
            String ipHeaderValue;
            if (isNotBlank(contextHttpRequest.getHeader(X_FORWARDED_FOR))) {
                ipHeaderValue = contextHttpRequest.getHeader(X_FORWARDED_FOR);
            } else {
                ipHeaderValue = contextHttpRequest.getRemoteAddr();
            }
            request.getHeaders().set(X_FORWARDED_FOR, ipHeaderValue);
            LOG.debug("Forward header for IP-allowlist checks: X-Forwarded-For: {}; URI: {}, under {}", ipHeaderValue,
                    request.getURI(), contextHttpRequest.getRequestURI());
        } else if (requestAttributes == null) {
            LOG.error("Token authentication failed, empty context, stackTrace: {}",
                    (Object) Thread.currentThread().getStackTrace());
        }

        return execution.execute(request, body);
    }
}
