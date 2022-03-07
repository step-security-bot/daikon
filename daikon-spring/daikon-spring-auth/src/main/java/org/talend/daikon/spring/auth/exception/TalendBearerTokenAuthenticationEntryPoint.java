package org.talend.daikon.spring.auth.exception;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Overrides response in case of failed authentication set originally by {@link BearerTokenAuthenticationEntryPoint}:
 * <p>
 * 1) response body is added
 * 2) WWW_AUTHENTICATE header only contains error code instead of the detailed error
 */
public class TalendBearerTokenAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    public TalendBearerTokenAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        String errorCode;

        if (authException instanceof OAuth2AuthenticationException) {
            OAuth2Error error = ((OAuth2AuthenticationException) authException).getError();
            errorCode = error.getErrorCode();
            if (error instanceof BearerTokenError) {
                status = ((BearerTokenError) error).getHttpStatus();
            }
        } else {
            errorCode = HttpStatus.UNAUTHORIZED.getReasonPhrase();
        }

        response.setStatus(status.value());

        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, String.format("Bearer error=\"%s\"", errorCode));
        response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        response.getWriter().write(getResponseBody(status, errorCode));
        response.getWriter().flush();

    }

    protected String getResponseBody(HttpStatus status, String errorDescription) throws JsonProcessingException {
        return mapper.writeValueAsString(ErrorResponse.builder().status(status.value()).title(errorDescription).build());
    }

}
