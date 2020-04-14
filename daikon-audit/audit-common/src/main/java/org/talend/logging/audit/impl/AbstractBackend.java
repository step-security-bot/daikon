package org.talend.logging.audit.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.talend.logging.audit.Context;
import org.talend.logging.audit.ContextBuilder;
import org.talend.logging.audit.LogLevel;

/**
 *
 */
public abstract class AbstractBackend {

    private static final char LOGGER_DELIM = '.';

    protected final String loggerPrefix;

    public AbstractBackend(String rootLogger) {
        this.loggerPrefix = rootLogger + LOGGER_DELIM;
    }

    public abstract void log(String category, LogLevel level, String message, Throwable throwable);

    public abstract Map<String, Object> getCopyOfContextMap();

    public abstract void setContextMap(Map<String, Object> newContext);

    public Map<String, Object> setNewContext(Map<String, Object> oldContext, Map<String, Object> newContext) {
        ContextBuilder builder = ContextBuilder.create();
        if (oldContext != null) {
            builder.with(oldContext);
        }
        if (newContext != null) {
            builder.with(newContext);
        }
        Context completeContext = builder.build();

        this.setContextMap(completeContext);
        return completeContext;
    }

    public Map<String, Object> setNewContext(Map<String, Object> oldContext) {
        ContextBuilder builder = ContextBuilder.create();
        if (oldContext != null) {
            builder.with(oldContext);
        }
        Context completeContext = builder.build();

        this.setContextMap(completeContext);
        return completeContext;
    }

    public void resetContext(Map<String, Object> oldContext) {
        this.setContextMap(oldContext == null ? new LinkedHashMap<>() : oldContext);
    }

}
