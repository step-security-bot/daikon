package org.talend.daikon.spring.auth.interceptor;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Http request interceptor to be used with RestTemplate
 * Propagates Bearer token to downstream services
 */
public class BearerTokenInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(BearerTokenInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        LOG.debug("Populating bearer token to downstream services");

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest contextHttpRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
            if (isNotBlank(contextHttpRequest.getHeader(HttpHeaders.AUTHORIZATION))) {
                LOG.debug("Set Authorization header with Bearer token");
                String tokenValue = contextHttpRequest.getHeader(HttpHeaders.AUTHORIZATION).substring("Bearer".length()).trim();
                request.getHeaders().setBearerAuth(tokenValue);
            }
        }

        return execution.execute(request, body);
    }
}
