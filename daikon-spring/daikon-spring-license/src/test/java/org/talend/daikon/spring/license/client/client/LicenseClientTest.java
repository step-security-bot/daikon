package org.talend.daikon.spring.license.client.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class LicenseClientTest {

    private static final String URL = "http://mylicense.com";

    private LicenseClient licenseClient;

    private RestTemplate restTemplateMock;

    @BeforeEach
    public void setUp() {
        restTemplateMock = mock(RestTemplate.class);
        licenseClient = new LicenseClient(URL, restTemplateMock);
    }

    @Test
    public void testGetConfig() {
        LicenseConfig expected = new LicenseConfig();
        expected.setKeepAliveInterval(45);
        when(restTemplateMock.exchange(eq(URL + "/v1/licenses/config"), eq(HttpMethod.GET), any(), eq(LicenseConfig.class)))
                .thenReturn(new ResponseEntity<LicenseConfig>(expected, HttpStatus.OK));
        LicenseConfig result = licenseClient.getConfig();
        assertEquals(expected, result);
    }

    @Test
    public void testKeepAlive() {
        licenseClient.keepAlive();
        verify(restTemplateMock, times(1)).exchange(eq(URL + "/v1/licenses/keepAlive"), eq(HttpMethod.POST), any(),
                eq(Void.class));
    }

    @Test
    public void testWithUrlNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LicenseClient(null, restTemplateMock);
        });
    }

    @Test
    public void testWithTemplateNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new LicenseClient(URL, null);
        });
    }
}
