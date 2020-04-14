package org.talend.logging.audit.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.talend.logging.audit.Context;

public class DefaultContextImpl extends LinkedHashMap<String, Object> implements Context {

    public DefaultContextImpl() {
        super(Collections.<String, String> emptyMap());
    }

    public DefaultContextImpl(Map<String, Object> context) {
        super(context);
    }

    @Override
    public String put(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

}
