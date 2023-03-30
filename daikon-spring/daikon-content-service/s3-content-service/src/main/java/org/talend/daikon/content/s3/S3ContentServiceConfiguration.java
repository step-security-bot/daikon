package org.talend.daikon.content.s3;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.diagnostics.FailureAnalyzer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.talend.daikon.content.ResourceResolver;
import org.talend.daikon.content.s3.provider.AmazonS3Provider;
import org.talend.daikon.content.s3.provider.S3BucketProvider;

import io.awspring.cloud.s3.S3PathMatchingResourcePatternResolver;
import io.awspring.cloud.s3.S3ProtocolResolver;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

@AutoConfiguration
@SuppressWarnings("InsufficientBranchCoverage")
@ConditionalOnProperty(name = "content-service.store", havingValue = "s3")
public class S3ContentServiceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3ContentServiceConfiguration.class);

    public static final String EC2_AUTHENTICATION = "EC2";

    public static final String TOKEN_AUTHENTICATION = "TOKEN";

    public static final String CUSTOM_AUTHENTICATION = "CUSTOM";

    public static final String MINIO_AUTHENTICATION = "MINIO";

    public static final String S3_ENDPOINT_URL = "content-service.store.s3.endpoint_url";

    public static final String S3_ENABLE_PATH_STYLE = "content-service.store.s3.enable_path_style";

    public static final String CONTENT_SERVICE_STORE_AUTHENTICATION = "content-service.store.s3.authentication";

    private static S3ClientBuilder configureEC2Authentication(S3ClientBuilder builder) {
        LOGGER.info("Using EC2 authentication");
        return builder.credentialsProvider(InstanceProfileCredentialsProvider.create());
    }

    private static S3ClientBuilder configureTokenAuthentication(Environment environment, S3ClientBuilder builder) {
        LOGGER.info("Using Token authentication");
        final String key = environment.getProperty("content-service.store.s3.accessKey");
        final String secret = environment.getProperty("content-service.store.s3.secretKey");
        AwsCredentials awsCredentials = AwsBasicCredentials.create(key, secret);
        return builder.credentialsProvider(StaticCredentialsProvider.create(awsCredentials));
    }

    private static boolean isMultiTenancyEnabled(Environment environment) {
        return environment.getProperty("multi-tenancy.s3.active", Boolean.class, Boolean.FALSE);
    }

    @Bean
    public S3Client amazonS3(Environment environment, ApplicationContext applicationContext) {
        // Configure authentication
        final String authentication = environment.getProperty(CONTENT_SERVICE_STORE_AUTHENTICATION, EC2_AUTHENTICATION)
                .toUpperCase();
        S3ClientBuilder builder = S3Client.builder();
        switch (authentication) {
        case EC2_AUTHENTICATION:
            builder = configureEC2Authentication(builder);
            builder = configurePathStyleAccess(environment, builder, false);
            break;
        case TOKEN_AUTHENTICATION:
            builder = configureTokenAuthentication(environment, builder);
            builder = configurePathStyleAccess(environment, builder, false);
            break;
        case MINIO_AUTHENTICATION:
            // Nothing to do to standard builder, but check "content-service.store.s3.endpoint_url" is set.
            if (!environment.containsProperty(S3_ENDPOINT_URL)) {
                throw new InvalidConfiguration("Missing '" + S3_ENDPOINT_URL + "' configuration");
            }
            builder = configurePathStyleAccess(environment, builder, true);
            break;
        case CUSTOM_AUTHENTICATION:
            try {
                final AmazonS3Provider amazonS3Provider = applicationContext.getBean(AmazonS3Provider.class);
                return amazonS3Provider.getS3Client();
            } catch (NoSuchBeanDefinitionException e) {
                throw new InvalidConfigurationMissingBean("No S3 client provider in context", AmazonS3Provider.class, e);
            }
        default:
            throw new IllegalArgumentException("Authentication '" + authentication + "' is not supported.");
        }

        // Configure region (optional)
        String strRegion = environment.getProperty("content-service.store.s3.region");
        if (StringUtils.isEmpty(strRegion)) {
            strRegion = Region.US_EAST_1.id();
        }
        builder = builder.region(Region.of(strRegion));

        // Configure endpoint url (optional)
        final String endpointUrl = environment.getProperty(S3_ENDPOINT_URL);
        if (StringUtils.isNotBlank(endpointUrl)) {
            builder = builder.endpointOverride(URI.create(endpointUrl));
        }

        // All set
        return builder.build();
    }

    private static S3ClientBuilder configurePathStyleAccess(Environment environment, S3ClientBuilder builder,
            boolean defaultValue) {
        final boolean enablePathStyle = environment.getProperty(S3_ENABLE_PATH_STYLE, Boolean.class, defaultValue);
        S3Configuration confBuilder = S3Configuration.builder().pathStyleAccessEnabled(enablePathStyle).build();
        builder.serviceConfiguration(confBuilder);
        return builder;
    }

    @Bean
    public ResourceResolver s3PathResolver(S3Client s3Client, Environment environment, ApplicationContext applicationContext,
            S3PathMatchingResourcePatternResolver resolver) {
        if (isMultiTenancyEnabled(environment)) {
            try {
                final S3BucketProvider s3BucketProvider = applicationContext.getBean(S3BucketProvider.class);
                return new S3ResourceResolver(resolver, s3Client, s3BucketProvider, environment);
            } catch (NoSuchBeanDefinitionException e) {
                throw new InvalidConfigurationMissingBean("No S3 bucket name provider in context", S3BucketProvider.class, e);
            }
        } else {
            final String staticBucketName = environment.getProperty("content-service.store.s3.bucket", String.class);
            final S3BucketProvider provider = new S3BucketProvider() {

                @Override
                public String getBucketName() {
                    return staticBucketName;
                }

                @Override
                public String getRoot() {
                    return StringUtils.EMPTY;
                }
            };
            return new S3ResourceResolver(resolver, s3Client, provider, environment);
        }
    }

    @Bean
    public S3PathMatchingResourcePatternResolver getPathMatchingResourcePatternResolver(S3Client s3Client,
            ApplicationContext context) {
        return new S3PathMatchingResourcePatternResolver(s3Client, simpleStorageResourceLoader(context));
    }

    private PathMatchingResourcePatternResolver simpleStorageResourceLoader(ApplicationContext context) {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        S3ProtocolResolver resolver = new S3ProtocolResolver();
        resourceLoader.addProtocolResolver(resolver);
        return new PathMatchingResourcePatternResolver(resourceLoader);
    }

    @Bean
    public FailureAnalyzer incorrectMultiTenant() {
        return new IncorrectS3ConfigurationAnalyzer();
    }

    class InvalidConfigurationMissingBean extends RuntimeException {

        private final Class missingBeanClass;

        InvalidConfigurationMissingBean(String message, Class missingBeanClass, Throwable cause) {
            super(message, cause);
            this.missingBeanClass = missingBeanClass;
        }

        Class getMissingBeanClass() {
            return missingBeanClass;
        }
    }

    class InvalidConfiguration extends RuntimeException {

        InvalidConfiguration(String message) {
            super(message);
        }

    }

}
