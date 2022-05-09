package org.talend.daikon.spring.mongo;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class TenantInformation {

    /**
     * Describes how to connect to db: host, port, replicaSet, credentials, etc.
     */
    private final MongoClientSettings clientSettings;

    /**
     * The database name to use to execute operation (write or read). Database name must <b>NOT</b> be empty.
     */
    private final String databaseName;

    /**
     * The Connection String URI for mongo connection. This is used as the cacheKey for the ReplicaSet connection strategy.
     * This must <b>Not</b> be empty in case of the ONE_PER_REPLICASET strategy.
     */
    private final String mongoUri;

    /**
     * The connection strategy is use to defined the mongo client connection caching mechanism.
     * In case of the onePerReplicaSet strategy, mongoUri is used as the cacheKey and for onePerTenant, combination of
     * the hosts, replicaSet, userName and database name
     */
    private final ConnectionStrategy mongoConnectionStrategy;

    @EqualsAndHashCode.Include
    private final String cacheKey;

    @Builder
    public TenantInformation(String databaseName, MongoClientSettings clientSettings, String mongoUri,
            ConnectionStrategy mongoConnectionStrategy) {
        Objects.requireNonNull(databaseName, "Database name must be present");
        Objects.requireNonNull(mongoConnectionStrategy, "Connection Strategy for mongo connection must be present.");
        if (mongoConnectionStrategy.equals(ConnectionStrategy.ONE_PER_REPLICASET)) {
            Objects.requireNonNull(mongoUri, "MongoUri must be present for onePerReplicaSet strategy.");
        }

        this.clientSettings = clientSettings;
        this.databaseName = databaseName;
        this.mongoUri = mongoUri;
        this.mongoConnectionStrategy = mongoConnectionStrategy;
        this.cacheKey = getKey(clientSettings, mongoUri);
    }

    private String getKey(MongoClientSettings clientSettings, String mongoUri) {
        switch (mongoConnectionStrategy) {
        case ONE_PER_REPLICASET:
            return mongoUri;
        case ONE_PER_TENANT:
        default:
            String hosts = getHost(clientSettings);
            String replicaSet = getReplicaSet(clientSettings);
            String userName = getUserName(clientSettings);

            return String.join(";", Arrays.asList(hosts, replicaSet, userName, databaseName));
        }
    }

    private String getHost(MongoClientSettings clientSettings) {
        return clientSettings.getClusterSettings().getHosts().stream().map(h -> h.getHost() + ":" + h.getPort())
                .collect(Collectors.joining(","));
    }

    private String getReplicaSet(MongoClientSettings clientSettings) {
        return clientSettings.getClusterSettings().getRequiredReplicaSetName();
    }

    private String getUserName(MongoClientSettings clientSettings) {
        return Optional.ofNullable(clientSettings.getCredential()).map(MongoCredential::getUserName).orElse("");
    }
}
