package org.talend.daikon.spring.mongo.info;

import java.util.Objects;

import org.talend.daikon.spring.mongo.TenantInformation;

import com.mongodb.MongoClientSettings;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class ReplicasetTenantInformation implements TenantInformation {

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

    @EqualsAndHashCode.Include
    private final String cacheKey;

    @Builder
    public ReplicasetTenantInformation(@Nonnull String databaseName, @Nonnull MongoClientSettings clientSettings,
            @Nonnull String mongoUri) {
        Objects.requireNonNull(databaseName, "Database name must be present");
        Objects.requireNonNull(mongoUri, "MongoUri must be present for onePerReplicaSet strategy.");

        this.clientSettings = clientSettings;
        this.databaseName = databaseName;
        this.mongoUri = mongoUri;
        this.cacheKey = mongoUri;
    }
}
