package org.talend.daikon.spring.license.client.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.web.client.RestTemplate;
import org.talend.daikon.spring.license.client.client.LicenseClient;

/**
 * @author agonzalez
 */
@AutoConfiguration
@ConditionalOnProperty(value = "iam.license.url")
public class LicenseConfiguration {

    @Bean
    public LicenseProperties licenseProperties() {
        return new LicenseProperties();
    }

    @Bean
    @ConditionalOnBean({ LicenseProperties.class, RestTemplate.class })
    public LicenseClient licenseClient(LicenseProperties licenseProperties, RestTemplate restTemplate) {
        return new LicenseClient(licenseProperties.getUrl(), restTemplate);
    }
}
