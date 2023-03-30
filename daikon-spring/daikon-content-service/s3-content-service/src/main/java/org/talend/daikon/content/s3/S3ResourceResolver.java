package org.talend.daikon.content.s3;

import static org.talend.daikon.content.s3.LocationUtils.S3PathBuilder.builder;
import static org.talend.daikon.content.s3.LocationUtils.toS3Location;
import static org.talend.daikon.content.s3.S3ContentServiceConfiguration.EC2_AUTHENTICATION;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.WritableResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.talend.daikon.content.AbstractResourceResolver;
import org.talend.daikon.content.DeletableResource;
import org.talend.daikon.content.s3.provider.S3BucketProvider;

import io.awspring.cloud.s3.S3Resource;
import io.micrometer.core.annotation.Timed;
import software.amazon.awssdk.services.s3.S3Client;

public class S3ResourceResolver extends AbstractResourceResolver {

    private final S3Client s3Client;

    private final S3BucketProvider bucket;

    private final Environment environment;

    S3ResourceResolver(ResourcePatternResolver delegate, S3Client s3Client, S3BucketProvider bucket, Environment environment) {
        super(delegate);
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.environment = environment;
    }

    @Timed
    @Override
    public DeletableResource[] getResources(String locationPattern) throws IOException {
        final String location = builder(bucket.getBucketName()) //
                .append(bucket.getRoot()) //
                .append(locationPattern) //
                .build();
        return super.getResources("s3://" + location);
    }

    @Timed
    @Override
    public DeletableResource getResource(String location) {
        if (location == null) {
            throw new IllegalArgumentException("Location can not be null");
        }
        final String cleanedUpLocation = location.trim();
        if (StringUtils.isEmpty(cleanedUpLocation)) {
            throw new IllegalArgumentException("Location can not be empty (was '" + location + "')");
        }

        final String s3Location = builder(bucket.getBucketName()) //
                .append(bucket.getRoot()) //
                .append(toS3Location(location)) //
                .build();

        return super.getResource("s3://" + s3Location);
    }

    @Override
    public String getLocationPrefix() {
        return builder(bucket.getBucketName()) //
                .append(bucket.getRoot()) //
                .build();
    }

    @Override
    protected DeletableResource convert(WritableResource writableResource) {
        final String authentication = environment
                .getProperty(S3ContentServiceConfiguration.CONTENT_SERVICE_STORE_AUTHENTICATION, EC2_AUTHENTICATION)
                .toUpperCase();
        String filename;
        if (writableResource instanceof io.awspring.cloud.s3.S3Resource) {
            filename = ((S3Resource) writableResource).getLocation().getObject();
        } else {
            filename = writableResource.getFilename();
        }
        final String bucketName = bucket.getBucketName();
        final String bucketRoot = bucket.getRoot();

        final S3DeletableResource resource = new S3DeletableResource(writableResource, s3Client, filename, bucketName,
                bucketRoot);
        switch (authentication) {
        case S3ContentServiceConfiguration.MINIO_AUTHENTICATION:
        case S3ContentServiceConfiguration.CUSTOM_AUTHENTICATION:
            final String host = environment.getProperty(S3ContentServiceConfiguration.S3_ENDPOINT_URL);
            final String s3Location = builder(bucketName) //
                    .append(bucketRoot) //
                    .append(toS3Location(filename)) //
                    .build();
            return new FixedURLS3Resource(host, s3Location, resource);
        default:
            return resource;
        }
    }
}