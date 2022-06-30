package org.talend.daikon.spring.license.client.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.talend.daikon.spring.license.client.client.LicenseClient;

public class LicenseConfigurationTest {

    private LicenseConfiguration configuration = new LicenseConfiguration();

    @Test
    public void testProperties() {
        assertNotNull(configuration.licenseProperties());
    }

    @Test
    public void testLicenseClient() {
        RestTemplate restTemplate = new RestTemplate();
        LicenseProperties properties = configuration.licenseProperties();
        properties.setUrl("http://bla.com");
        LicenseClient licenseClient = configuration.licenseClient(properties, restTemplate);
        assertNotNull(licenseClient);
    }
}
