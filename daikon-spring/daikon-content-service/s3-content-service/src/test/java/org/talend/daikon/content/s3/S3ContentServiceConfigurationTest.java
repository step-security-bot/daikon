package org.talend.daikon.content.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;

public class S3ContentServiceConfigurationTest {

    private S3ContentServiceConfiguration configuration = new S3ContentServiceConfiguration();

    @Test
    public void shouldCheckEndpointURLWhenUsingMinio() {
        assertThrows(S3ContentServiceConfiguration.InvalidConfiguration.class, () -> {
            // given
            final Environment environment = mock(Environment.class);
            final ApplicationContext context = mock(ApplicationContext.class);

            when(environment.getProperty(eq("content-service.store.s3.authentication"), anyString())).thenReturn("MINIO");

            // when
            configuration.amazonS3(environment, context);
        });
    }

    @Test
    public void shouldUseEndpointURLWhenUsingMinio() {
        // given
        final Environment environment = mock(Environment.class);
        final ApplicationContext context = mock(ApplicationContext.class);

        when(environment.getProperty(eq("content-service.store.s3.authentication"), anyString())).thenReturn("MINIO");
        when(environment.containsProperty(eq(S3ContentServiceConfiguration.S3_ENDPOINT_URL))).thenReturn(true);
        when(environment.getProperty(eq(S3ContentServiceConfiguration.S3_ENDPOINT_URL))).thenReturn("http://fake.io:9001");
        when(environment.getProperty(eq(S3ContentServiceConfiguration.S3_ENABLE_PATH_STYLE), eq(Boolean.class), any()))
                .thenReturn(true);

        // when
        final S3Client s3Client = configuration.amazonS3(environment, context);

        GetUrlRequest request = GetUrlRequest.builder().endpoint(URI.create("http://fake.io:9001")).bucket("mybucket")
                .key("file.csv").build();

        final String fileUrl = s3Client.utilities().getUrl(request).toString();
        assertEquals("http://fake.io:9001/mybucket/file.csv", fileUrl);
    }

    @Test
    public void shouldUseConfigurationWhenUsingToken() {
        // given
        final Environment environment = mock(Environment.class);
        final ApplicationContext context = mock(ApplicationContext.class);

        when(environment.getProperty(eq("content-service.store.s3.authentication"), anyString())).thenReturn("TOKEN");
        when(environment.getProperty("content-service.store.s3.secretKey")).thenReturn("verySecret");
        when(environment.getProperty("content-service.store.s3.accessKey")).thenReturn("anAccessKey");
        when(environment.containsProperty(eq(S3ContentServiceConfiguration.S3_ENDPOINT_URL))).thenReturn(true);
        when(environment.getProperty(eq(S3ContentServiceConfiguration.S3_ENDPOINT_URL))).thenReturn("http://fake.io:9001");
        when(environment.getProperty(eq(S3ContentServiceConfiguration.S3_ENABLE_PATH_STYLE), eq(Boolean.class), any()))
                .thenReturn(false);

        // when
        configuration.amazonS3(environment, context);

        // then
        verify(environment, times(1)).getProperty(eq("content-service.store.s3.secretKey"));
        verify(environment, times(1)).getProperty(eq("content-service.store.s3.accessKey"));
    }
}