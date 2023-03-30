package org.talend.daikon.spring.mongo;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.talend.daikon.spring.mongo.info.ReplicasetTenantInformation;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;

public class CachedMongoClientPerReplicaSetTest {

    private static MongoServer server1;

    private static MongoServer server2;

    private final CachedMongoClientProvider cachedMongoClientProvider = new CachedMongoClientProvider(1, TimeUnit.SECONDS);

    private static InetSocketAddress serverAddress1;

    private static InetSocketAddress serverAddress2;

    private static final TenantInformationProvider TENANT1 = getTenantInformationProvider("Tenant1");

    private static final TenantInformationProvider TENANT2 = getTenantInformationProvider("Tenant2");

    private static final TenantInformationProvider TENANT3 = getTenantInformationProvider("Tenant3");

    private static TenantInformationProvider getTenantInformationProvider(final String tenant) {
        return () -> {
            String mongoUri = getMongoUri(tenant);
            ConnectionString connectionString = new ConnectionString(mongoUri);
            return ReplicasetTenantInformation.builder()
                    .clientSettings(MongoClientSettings.builder().applyConnectionString(connectionString).build())
                    .databaseName(tenant).mongoUri(connectionString.getConnectionString()).build();
        };
    }

    @BeforeAll
    public static void setUp() {
        server1 = new MongoServer(new MemoryBackend());
        // bind on a random local port
        serverAddress1 = server1.bind();

        server2 = new MongoServer(new MemoryBackend());
        // bind on a random local port
        serverAddress2 = server2.bind();
    }

    @AfterAll
    public static void tearDown() {
        server1.shutdown();
        server2.shutdown();
    }

    private static String getMongoUri(String tenant) {
        switch (tenant) {
        case "Tenant3":
            return "mongodb://" + serverAddress2.getHostName() + ":" + serverAddress2.getPort();
        case "Tenant1":
        case "Tenant2":
        default:
            return "mongodb://" + serverAddress1.getHostName() + ":" + serverAddress1.getPort();

        }
    }

    @Test
    public void shouldNotEvictInstanceBeforeTimeout() {
        // When
        final MongoClient client1 = cachedMongoClientProvider.get(TENANT1);
        final MongoClient client2 = cachedMongoClientProvider.get(TENANT1);

        // Then
        assertSame(client1, client2);
    }

    @Test
    public void shouldEvictInstanceAfterTimeout() throws Exception {
        // When
        final MongoClient client1 = cachedMongoClientProvider.get(TENANT1);
        TimeUnit.SECONDS.sleep(2);
        final MongoClient client2 = cachedMongoClientProvider.get(TENANT1);

        // Then
        assertNotSame(client1, client2);
    }

    @Test
    public void shouldCreateClientForReplicaSet() {
        // When
        final MongoClient client1 = cachedMongoClientProvider.get(TENANT1);
        final MongoClient client2 = cachedMongoClientProvider.get(TENANT2);
        final MongoClient client3 = cachedMongoClientProvider.get(TENANT3);

        // Then
        assertSame(client1, client2);
        assertNotSame(client1, client3);
    }

    @Test
    public void shouldCloseClient() {
        // Given
        final TenantInformationProvider tenantInformationProvider = getTenantInformationProvider("Tenant1");

        // When
        final MongoClient client1 = cachedMongoClientProvider.get(tenantInformationProvider);
        cachedMongoClientProvider.close(tenantInformationProvider);
        final MongoClient client2 = cachedMongoClientProvider.get(tenantInformationProvider);

        // Then
        assertNotSame(client1, client2);
    }
}
