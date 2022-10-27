package org.talend.daikon.spring.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.talend.daikon.spring.auth.introspection.factory.UserDetailsIntrospectorFactory;
import org.talend.daikon.spring.auth.introspection.factory.UserDetailsIntrospectorFactoryOnPrem;
import org.talend.iam.im.scim.client.MeClient;
import org.talend.iam.im.scim.client.autoconfigure.HttpClientProperties;
import org.talend.iam.im.scim.client.autoconfigure.HttpClientSupport;

@Configuration
@ConditionalOnProperty("iam.scim.url")
public class AuthAutoConfigurationOnPrem {

    @Value("${iam.scim.url}")
    private String url;

    @Bean
    @ConditionalOnMissingBean(MeClient.class)
    @ConditionalOnBean(RestTemplate.class)
    public MeClient scimMeClient(RestTemplate restTemplate, HttpClientSupport httpClientSupport) {
        MeClient answer = new MeClient(url, restTemplate);
        HttpClientProperties properties = httpClientSupport.getHttpClientProperties();
        answer.setRetryParams(properties.getRetryAttempts(), properties.getRetryDelay());
        return answer;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.security.oauth2.resourceserver.iam.opaque-token.query-entitlements", havingValue = "true", matchIfMissing = false)
    public UserDetailsIntrospectorFactory userDetailsIntrospectorFactory(MeClient meClient) {
        return new UserDetailsIntrospectorFactoryOnPrem(meClient);
    }
}
