package org.talend.daikon.spring.license;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.talend.daikon.spring.license.client.autoconfigure.LicenseConfiguration;
import org.talend.daikon.spring.license.client.client.LicenseClient;
import org.talend.daikon.spring.license.filter.KeepAliveLicenseFilter;

@AutoConfiguration
@AutoConfigureAfter(LicenseConfiguration.class)
public class KeepAliveConfiguration {

    /**
     * Needs to be executed after OAuth2TokenRelayFilter
     */
    private static final int KEEP_ALIVE_FILTER_ORDER = 15;

    @Bean
    public FilterRegistrationBean<KeepAliveLicenseFilter> keepAliveLicenseFilter(LicenseClient licenseClient) {
        FilterRegistrationBean<KeepAliveLicenseFilter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter(new KeepAliveLicenseFilter(licenseClient));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(KEEP_ALIVE_FILTER_ORDER);

        return registrationBean;
    }
}
