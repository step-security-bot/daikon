package org.talend.daikon.spring.license.client.client;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Configuration for license service visible to license clients
 */
@JsonInclude(NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LicenseConfig implements Serializable {

    private static final long serialVersionUID = -4961988688700151834L;

    private long keepAliveInterval;

    /**
     * Interval in seconds at which keepAlive endpoint must be called by license service clients.
     */
    public long getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public void setKeepAliveInterval(long keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }
}
