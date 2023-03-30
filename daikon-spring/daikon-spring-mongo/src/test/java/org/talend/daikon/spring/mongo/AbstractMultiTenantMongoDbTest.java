package org.talend.daikon.spring.mongo;

import static org.talend.daikon.spring.mongo.TestMultiTenantConfiguration.changeTenant;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import de.bwaldvogel.mongo.MongoServer;

@ExtendWith(SpringExtension.class)
@EnableMongoRepositories
@ComponentScan("org.talend.daikon.spring.mongo")
@ContextConfiguration(classes = MultiTenantMongoDbFactoryTest.class)
public abstract class AbstractMultiTenantMongoDbTest {

    @Autowired
    private MongoServer mongoServer;

    @Autowired
    private MongoClientProvider mongoClientProvider;

    @Autowired
    private TenantInformationProvider tenantInformationProvider;

    @BeforeEach
    public void tearDown() throws IOException {
        // Drop all created databases during test
        MongoClient client = MongoClients
                .create(new ConnectionString("mongodb:/" + mongoServer.getLocalAddress().toString()) + "/standard");
        for (String database : client.listDatabaseNames()) {
            client.getDatabase(database).drop();
        }
        // Switch back to default tenant
        changeTenant("default");
        // Clean up mongo clients
        mongoClientProvider.close();
    }
}
