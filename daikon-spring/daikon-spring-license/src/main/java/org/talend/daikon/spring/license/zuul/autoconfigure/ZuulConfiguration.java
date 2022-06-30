package org.talend.daikon.spring.license.zuul.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.talend.daikon.spring.license.client.client.LicenseClient;
import org.talend.daikon.spring.license.client.autoconfigure.LicenseConfiguration;
import org.talend.daikon.spring.license.zuul.filters.pre.KeepAliveLicenseFilter;

import com.netflix.zuul.ZuulFilter;

/**
 * @author agonzalez
 */
@Configuration
@ConditionalOnClass(ZuulFilter.class)
@AutoConfigureAfter(LicenseConfiguration.class)
public class ZuulConfiguration {

    @Conditional(ConditionOnOidcZuulProxyAndLicenseClient.class)
    @ConditionalOnWebApplication
    @Bean
    public KeepAliveLicenseFilter zuulKeepAliveLicenseFilter(LicenseClient licenseClient) {
        return new KeepAliveLicenseFilter(licenseClient);
    }

    static class ConditionOnOidcZuulProxyAndLicenseClient extends AllNestedConditions {

        ConditionOnOidcZuulProxyAndLicenseClient() {

            super(ConfigurationPhase.REGISTER_BEAN);
        }

        @ConditionalOnBean(LicenseClient.class)
        static class OnLicenseClient {
        }

        @ConditionalOnClass(value = { ZuulFilter.class,
                SecurityProperties.class }, name = "org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client")
        static class OnOidcZuulProxy {
        }
    }
}
