package org.talend.daikon.spring.auth.cache;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CacheExceptionHandlerWrapper implements Cache {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheExceptionHandlerWrapper.class);

    private Cache delegate;

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(Object key) {
        LOGGER.debug("Cache GET operation: {}", key);
        try {
            return delegate.get(key);
        } catch (Exception ex) {
            LOGGER.warn("Exception during Redis cache GET operation: {}", ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        LOGGER.debug("Cache GET operation key = {} with type = {}", key, type.getName());
        try {
            return delegate.get(key, type);
        } catch (Exception ex) {
            LOGGER.warn("Exception during Redis cache GET operation for type {}: {}", type.getName(), ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Callable<T> valueLoader) {
        LOGGER.debug("Cache GET operation key = {} with valueLoader = {}", key, valueLoader.getClass().getName());
        try {
            ValueWrapper cachedValue = delegate.get(key);

            if (cachedValue != null) {
                return (T) cachedValue.get();
            }

            T value = loadValueFromOriginalSource(key, valueLoader);
            put(key, value);

            return value;
        } catch (Exception ex) {
            LOGGER.warn("Exception during Redis cache GET operation: {}. Value will be retrieved with valueLoader.",
                    ex.getMessage(), ex);
            return loadValueFromOriginalSource(key, valueLoader); // no put to avoid second timeout
        }
    }

    private <T> T loadValueFromOriginalSource(Object key, Callable<T> valueLoader) {
        try {
            return valueLoader.call();
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(Object key, Object value) {
        LOGGER.debug("Cache put operation {}: {}", key, value);
        try {
            delegate.put(key, value);
        } catch (Exception ex) {
            LOGGER.warn("Exception during Redis cache PUT operation: {}", ex.getMessage(), ex);
        }
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        LOGGER.debug("Cache putIfAbsent operation {}: {}", key, value);
        try {
            return delegate.putIfAbsent(key, value);
        } catch (Exception ex) {
            LOGGER.warn("Exception during Redis cache putIfAbsent operation: {}", ex.getMessage(), ex);
            return null;
        }
    }

    @Override
    public void evict(Object key) {
        delegate.evict(key);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        return delegate.evictIfPresent(key);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean invalidate() {
        return delegate.invalidate();
    }
}
