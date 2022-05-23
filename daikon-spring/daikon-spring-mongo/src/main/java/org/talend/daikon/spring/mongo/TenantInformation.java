package org.talend.daikon.spring.mongo;

import com.mongodb.MongoClientSettings;

public interface TenantInformation {

    String getDatabaseName();

    MongoClientSettings getClientSettings();
}
