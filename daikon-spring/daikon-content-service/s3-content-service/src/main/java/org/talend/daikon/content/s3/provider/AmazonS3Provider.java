package org.talend.daikon.content.s3.provider;

import software.amazon.awssdk.services.s3.S3Client;

/**
 * An interface to be implemented to supply Amazon S3 clients. Examples of implementation include:
 * <ul>
 * <li>Static bucket name (read from {@link org.springframework.core.env.Environment}).</li>
 * <li>Runtime-defined bucket name (for multi tenant use cases).</li>
 * </ul>
 */
@FunctionalInterface
public interface AmazonS3Provider {

    /**
     * @return A configured {@link S3Client S3 client} ready for use.
     */
    S3Client getS3Client();
}
