package org.talend.daikon.spring.audit.logs.service;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

public class AuditLogUrlExtractorImpl implements AuditLogUrlExtractor {

    private static final String FORWARDED_HOST_HEADER = "x-forwarded-host";

    private static final String FORWARDED_PROTO_HEADER = "x-forwarded-proto";

    private static final String ENVOY_ORIGINAL_PATH_HEADER = "x-envoy-original-path";

    @Override
    public String extract(HttpServletRequest httpServletRequest) {
        if (!StringUtils.isEmpty(httpServletRequest.getHeader(FORWARDED_HOST_HEADER))) {
            return UriComponentsBuilder
                    // Use x-envoy-original-path header for path if possible
                    .fromPath(Optional.ofNullable(httpServletRequest.getHeader(ENVOY_ORIGINAL_PATH_HEADER)) //
                            .orElse(httpServletRequest.getRequestURI()))
                    // Use x-forwarded-proto header for protocol if possible
                    .scheme(Optional.ofNullable(httpServletRequest.getHeader(FORWARDED_PROTO_HEADER)) //
                            .filter(it -> it.matches("http|https")).orElse("https"))
                    // Use x-forwarded-host header for host
                    .host(retrieveHost(httpServletRequest)) //
                    .query(httpServletRequest.getQueryString()).build().toUri().toString();
        } else {
            return httpServletRequest.getRequestURL().toString();
        }
    }

    private String retrieveHost(HttpServletRequest httpServletRequest) {
        String hostWithPort = httpServletRequest.getHeader(FORWARDED_HOST_HEADER);
        return hostWithPort.split(":")[0];
    }
}
