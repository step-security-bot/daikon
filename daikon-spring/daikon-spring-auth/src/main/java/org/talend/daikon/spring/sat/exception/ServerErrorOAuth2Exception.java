package org.talend.daikon.spring.sat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;

/**
 * Exception thrown in case of unexpected errors during authentication
 */
public class ServerErrorOAuth2Exception extends OAuth2Exception {

    public ServerErrorOAuth2Exception(String errorDescription, Throwable t) {
        super(errorDescription, t);
    }

    @Override
    public String getOAuth2ErrorCode() {
        return OAuth2ErrorCodes.SERVER_ERROR;
    }

    @Override
    public int getHttpErrorCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

}
