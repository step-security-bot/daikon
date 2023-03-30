package org.talend.daikon.mongo;

import java.util.Objects;

import com.mongodb.MongoClientSettings;

import lombok.Getter;

@Getter
public class TenantInformation {

    private final String databaseName;

    private final MongoClientSettings settings;

    public TenantInformation(String databaseName, MongoClientSettings settings) {
        Objects.requireNonNull(databaseName, "Database name must be present");

        this.settings = settings;
        this.databaseName = databaseName;
    }

}
