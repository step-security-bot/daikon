package org.talend.daikon.spring.auth.exception;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Overrides behavior in case of failed authentication (handled originally by {@link BearerTokenAuthenticationEntryPoint}):
 * Delegates exception handling to {@link AuthExceptionHandler}
 */
public class TalendBearerTokenAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(TalendBearerTokenAuthenticationEntryPoint.class);

    private final HandlerExceptionResolver handlerExceptionResolver;

    public TalendBearerTokenAuthenticationEntryPoint(HandlerExceptionResolver handlerExceptionResolver) {
        if (null == handlerExceptionResolver) {
            LOGGER.warn("HandlerExceptionResolver is `null`, exceptions won't be handled properly"); // may happen in unit tests
        }
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        LOGGER.debug("Pass authException {} to handlerExceptionResolver", authException.getClass().getCanonicalName());
        handlerExceptionResolver.resolveException(request, response, null, authException);
    }

}
