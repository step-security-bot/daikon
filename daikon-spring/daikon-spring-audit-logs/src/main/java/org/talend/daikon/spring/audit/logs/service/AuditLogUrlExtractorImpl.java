package org.talend.daikon.spring.audit.logs.service;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.UriComponentsBuilder;

public class AuditLogUrlExtractorImpl implements AuditLogUrlExtractor {

    private static final String HOST_HEADER = "host";

    private static final String FORWARDED_HOST_HEADER = "x-forwarded-host";

    private static final String FORWARDED_PROTO_HEADER = "x-forwarded-proto";

    private static final String ENVOY_ORIGINAL_PATH_HEADER = "x-envoy-original-path";

    @Override
    public String extract(HttpServletRequest httpServletRequest) {
        return UriComponentsBuilder
                // Use x-envoy-original-path header for path if possible
                .fromPath(Optional.ofNullable(httpServletRequest.getHeader(ENVOY_ORIGINAL_PATH_HEADER)) //
                        .orElse(httpServletRequest.getRequestURI()))
                // Use x-forwarded-proto header for protocol if possible
                .scheme(Optional.ofNullable(httpServletRequest.getHeader(FORWARDED_PROTO_HEADER)) //
                        .filter(it -> it.matches("http|https")) //
                        .orElse(httpServletRequest.getScheme()))
                // Use x-forwarded-host or host header for host if possible
                .host(extractHost(Optional.ofNullable(httpServletRequest.getHeader(FORWARDED_HOST_HEADER)) //
                        .orElse(Optional.ofNullable(httpServletRequest.getHeader(HOST_HEADER)) //
                                .orElse(httpServletRequest.getServerName())))) //
                // Add query strings
                .query(httpServletRequest.getQueryString()).build().toUri().toString();
    }

    // Clean host in case it contains a port number (e.g. my-awesome-host:8080)
    private String extractHost(String host) {
        return host.split(":")[0];
    }
}
