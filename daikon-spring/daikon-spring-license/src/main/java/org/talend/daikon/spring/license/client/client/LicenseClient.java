package org.talend.daikon.spring.license.client.client;

import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * @author agonzalez
 */
public class LicenseClient {

    private final RestTemplate restTemplate;

    private String url;

    private Function<Supplier, Object> exceptionHandler;

    public LicenseClient(String url, RestTemplate restTemplate) {
        super();
        if (restTemplate == null) {
            throw new IllegalArgumentException("restTemplate argument is required");
        }
        if (url == null) {
            throw new IllegalArgumentException("url argument is required");
        }
        this.url = url + "/v1/licenses";
        this.restTemplate = restTemplate;
        this.exceptionHandler = new DefaultExceptionHandler();
    }

    public LicenseConfig getConfig() {
        return (LicenseConfig) exceptionHandler.apply(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity entity = new HttpEntity<>(headers);
            ResponseEntity<LicenseConfig> response = restTemplate.exchange(url + "/config", HttpMethod.GET, entity,
                    LicenseConfig.class);
            return response.getBody();
        });
    }

    public void keepAlive() {
        exceptionHandler.apply(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity entity = new HttpEntity<>(headers);
            return restTemplate.exchange(url + "/keepAlive", HttpMethod.POST, entity, Void.class);
        });
    }

    /**
     * Handles server errors (converts HttpClientErrorException to
     * SCIMException).
     */
    public static class DefaultExceptionHandler implements Function<Supplier, Object> {

        public DefaultExceptionHandler() {
        }

        /**
         * Handles server errors (converts HttpClientErrorException to
         * EmptyResultDataAccessException).
         *
         * @param supplier this function calls the REST Service
         * @return the value returned byt the REST Service
         * @throws EmptyResultDataAccessException if the requested resource was not found
         */
        @Override
        public Object apply(Supplier supplier) {
            try {
                return supplier.get();
            } catch (HttpStatusCodeException e) {
                if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    throw new BadCredentialsException(e.getMessage(), e);
                }
                throw e;
            }
        }
    }
}
