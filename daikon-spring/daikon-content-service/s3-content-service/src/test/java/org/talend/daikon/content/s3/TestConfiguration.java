package org.talend.daikon.content.s3;

import java.io.File;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.util.TestSocketUtils;
import org.talend.daikon.content.s3.provider.AmazonS3Provider;
import org.talend.daikon.content.s3.provider.S3BucketProvider;

import io.findify.s3mock.S3Mock;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@AutoConfiguration
public class TestConfiguration implements InitializingBean, DisposableBean {

    final static AtomicInteger clientNumber = new AtomicInteger(0);

    private S3Mock s3Mock;

    private int s3MockPort;

    @Bean
    public AmazonS3Provider amazonS3Provider(S3Client s3Client) {
        return () -> s3Client;
    }

    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder();
        builder.credentialsProvider(AnonymousCredentialsProvider.create());
        builder.endpointOverride(URI.create("http://127.0.0.1:" + s3MockPort));

        final S3Client s3Client = builder.build();

        s3Client.createBucket(CreateBucketRequest.builder().bucket("s3-content-service1").build());
        s3Client.createBucket(CreateBucketRequest.builder().bucket("s3-content-service2").build());

        // Amazon S3 reads region from endpoint (127.0.0.1...)
        return s3Client;
    }

    @Bean
    public S3BucketProvider s3BucketProvider() {
        return new S3BucketProvider() {

            @Override
            public String getBucketName() {
                if (clientNumber.get() == 0) {
                    return "s3-content-service1";
                } else {
                    return "s3-content-service2";
                }
            }

            @Override
            public String getRoot() {
                if (clientNumber.get() == 0) {
                    return "app1";
                } else if (clientNumber.get() == 1) {
                    return "app2";
                } else {
                    return "";
                }
            }
        };
    }

    @Override
    public void afterPropertiesSet() {
        s3MockPort = TestSocketUtils.findAvailableTcpPort();
        s3Mock = S3Mock.create(s3MockPort, new File(".").getAbsolutePath() + "/target/s3");
        s3Mock.start();
    }

    @Override
    public void destroy() {
        s3Mock.stop();
    }
}
