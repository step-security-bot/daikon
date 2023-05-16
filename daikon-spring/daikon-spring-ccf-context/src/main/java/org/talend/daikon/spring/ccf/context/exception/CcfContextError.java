package org.talend.daikon.spring.ccf.context.exception;

public class CcfContextError extends RuntimeException {

    public CcfContextError(String errorMessage) {
        super(errorMessage);
    }
}
