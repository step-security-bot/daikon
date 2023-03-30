package org.talend.daikon.content.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.talend.daikon.content.DeletableResource;
import org.talend.daikon.content.DeletableResourceTest;

public class S3DeletableResourceTest extends DeletableResourceTest {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        TestConfiguration.clientNumber.set(0);
    }

    @Test
    public void shouldNotThrowErrorWhenWriteAfterClose() throws Exception {
        // Given
        OutputStream outputStream = resource.getOutputStream();
        outputStream.write("1234".getBytes());
        outputStream.close();

        // When
        outputStream.write('a'); // No exception to be thrown

        // Then
        assertEquals("1234", IOUtils.toString(resource.getInputStream()));
    }

    @Override
    public String getUrlProtocol() {
        return "https";
    }

    @Override
    public String getURIScheme() {
        return "https";
    }

    @Test
    public void shouldGetFile() throws Exception {
        assertThrows(UnsupportedOperationException.class, () -> {
            resource.getFile(); // Not supported on S3
        });
    }

    @Test
    public void lastModifiedShouldBeComputed() throws Exception {
        // Not implemented by S3 mock. so do nothing
    }

    @Override
    public void getFilename() {
        assertEquals("app1/" + LOCATION, resource.getFilename());
    }

    @Test
    public void shouldGetDescription() {
        assertEquals("Location{bucket='s3-content-service1', object='app1/file.txt', version='null'}", resource.getDescription());
    }

    @Test
    public void shouldGetDescriptionInMultiTenant() {
        // Given
        TestConfiguration.clientNumber.set(0);
        final Resource resource1 = resolver.getResource(LOCATION);

        // Then
        assertEquals("Location{bucket='s3-content-service1', object='app1/file.txt', version='null'}",
                resource1.getDescription());

        // Given
        TestConfiguration.clientNumber.set(1);
        final Resource resource2 = resolver.getResource(LOCATION);

        // Then
        assertEquals("Location{bucket='s3-content-service2', object='app2/file.txt', version='null'}",
                resource2.getDescription());

        // Given
        TestConfiguration.clientNumber.set(2);
        final Resource resource3 = resolver.getResource(LOCATION);

        // Then
        assertEquals("Location{bucket='s3-content-service2', object='file.txt', version='null'}", resource3.getDescription());
    }

    @Test
    public void shouldStoreUsingMultiTenantClient() throws Exception {
        // Given
        TestConfiguration.clientNumber.set(0);
        final DeletableResource resource1 = resolver.getResource("mockS3_1.txt");
        try (OutputStream outputStream = resource1.getOutputStream()) {
            outputStream.write("test_tenant1".getBytes());
        }

        TestConfiguration.clientNumber.set(1);
        final DeletableResource resource2 = resolver.getResource("mockS3_2.txt");
        try (OutputStream outputStream = resource2.getOutputStream()) {
            outputStream.write("test_tenant2".getBytes());
        }

        // Then
        TestConfiguration.clientNumber.set(0);
        assertTrue(resolver.getResource("mockS3_1.txt").exists());
        assertFalse(resolver.getResource("mockS3_2.txt").exists());

        TestConfiguration.clientNumber.set(1);
        assertFalse(resolver.getResource("mockS3_1.txt").exists());
        assertTrue(resolver.getResource("mockS3_2.txt").exists());
    }
}