package org.talend.daikon.spring.mongo;

import java.util.Arrays;

public enum ConnectionStrategy {
    ONE_PER_TENANT("onePerTenant"),
    ONE_PER_REPLICASET("onePerReplicaSet");

    private final String strategy;

    ConnectionStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getStrategy() {
        return strategy;
    }

    /**
     * @return the Enum representation for the given string.
     * @throws IllegalArgumentException if unknown string.
     */
    public static ConnectionStrategy fromString(String s) throws IllegalArgumentException {
        return Arrays.stream(ConnectionStrategy.values()).filter(v -> v.strategy.equals(s)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown value: " + s));
    }
}
