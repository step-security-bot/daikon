package org.talend.daikon.spring.auth.common.model.userdetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

public class UserDetailsConverterTest {

    private static ObjectMapper mapper = new ObjectMapper();

    @Test
    @SneakyThrows
    public void convertPatIntrospectionSuccess() {
        InputStream resourceAsStream = getClass().getResourceAsStream("/converter/pat-introspection-success.json");
        Map<String, Object> introspectionResult = mapper.readValue(resourceAsStream, new TypeReference<Map<String, Object>>() {});

        AuthUserDetails result = UserDetailsConverter.convert(introspectionResult);

        assertEquals("befb746d-10b2-442e-974c-896d26379573", result.getId());
        assertEquals("mbutko@mbutko01.us.talend.com", result.getUsername());
        assertEquals("User Talend", result.getName());
        assertEquals("user@yopmails.com", result.getEmail());
        assertEquals("9133741e-d49d-4cd8-a09e-9791fead2583", result.getTenantId());
        assertEquals("mbutko01.us.talend.com", result.getTenantName());
        assertEquals(new HashSet<>(Arrays.asList("CRAWLING_CREATE", "TMC_OPERATOR", "DATASET_READ")),
                     result.getEntitlements());
        assertEquals(Arrays.asList("48e38a59-ce6f-497a-84b9-63598ff8304b", "914413da-6a86-4a5f-b753-a963d1916179"),
                            result.getGroupIds());
        assertNull(result.getMiddleName());
    }

    @Test
    @SneakyThrows
    public void convertPatIntrospectionWithMiddleName() {
        InputStream resourceAsStream = getClass().getResourceAsStream("/converter/pat-introspection-success-with-middle-name.json");
        Map<String, Object> introspectionResult = mapper.readValue(resourceAsStream, new TypeReference<Map<String, Object>>() {});

        AuthUserDetails result = UserDetailsConverter.convert(introspectionResult);

        assertEquals("befb746d-10b2-442e-974c-896d26379573", result.getId());
        assertEquals("mbutko@mbutko01.us.talend.com", result.getUsername());
        assertEquals("User Something Talend", result.getName());
        assertEquals("Something", result.getMiddleName());
        assertEquals("user@yopmails.com", result.getEmail());
        assertEquals("9133741e-d49d-4cd8-a09e-9791fead2583", result.getTenantId());
        assertEquals("mbutko01.us.talend.com", result.getTenantName());
        assertEquals(new HashSet<>(Arrays.asList("CRAWLING_CREATE", "TMC_OPERATOR", "DATASET_READ")),
                     result.getEntitlements());
        assertEquals(Arrays.asList("48e38a59-ce6f-497a-84b9-63598ff8304b", "914413da-6a86-4a5f-b753-a963d1916179"),
                            result.getGroupIds());
    }

    @Test
    @SneakyThrows
    public void convertJwtClaimsSuccess() {
        InputStream resourceAsStream = getClass().getResourceAsStream("/converter/jwt-claims-success.json");
        Map<String, Object> introspectionResult = mapper.readValue(resourceAsStream, new TypeReference<Map<String, Object>>() {});

        AuthUserDetails result = UserDetailsConverter.convert(introspectionResult);

        assertEquals("8e19e56b-4844-454b-8d0f-17d4be7f057e", result.getId());
        assertEquals("user.mbutko.1.yopmail.com@mbutko01.us.talend.com", result.getUsername());
        assertEquals("user test", result.getName());
        assertEquals("user.email.ut@yopmail.com", result.getEmail());
        assertEquals("9133741e-d49d-4cd8-a09e-9791fead2583", result.getTenantId());
        assertEquals("mbutko01.us.talend.com", result.getTenantName());
        assertTrue(result.getEntitlements().isEmpty());
        assertTrue(result.getGroupIds().isEmpty());
        assertNull(result.getMiddleName());
    }

    @Test
    @SneakyThrows
    public void convertPatIntrospectionWithoutUsernameClaimsFail() {
        InputStream resourceAsStream = getClass().getResourceAsStream("/converter/pat-introspection-fail.json");
        Map<String, Object> introspectionResult = mapper.readValue(resourceAsStream, new TypeReference<Map<String, Object>>() {});

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                          () -> UserDetailsConverter.convert(introspectionResult));
        assertEquals("Username not found", exception.getMessage());
    }

    @Test
    @SneakyThrows
    public void convertClientCredentialsJwtClaimsSuccess() {
        InputStream resourceAsStream = getClass().getResourceAsStream("/converter/jwt-cc-claims-success.json");
        Map<String, Object> introspectionResult = mapper.readValue(resourceAsStream, new TypeReference<Map<String, Object>>() {});

        AuthUserDetails result = UserDetailsConverter.convert(introspectionResult);

        assertEquals("qa-provisioner-client-id", result.getId());
        assertEquals("QA Provisioner - Client Credentials Flow", result.getUsername());
        assertEquals(new HashSet<>(Arrays.asList("TC_ACCOUNT_CREATE", "TC_ACCOUNT_READ", "TC_ACCOUNT_STATUS_READ")),
                     result.getEntitlements());
        assertNull(result.getTenantId());
    }

    @Test
    @SneakyThrows
    public void convertClientCredentialsJwtWithoutClientNameFails() {
        InputStream resourceAsStream = getClass().getResourceAsStream("/converter/jwt-cc-claims-no-name.json");
        Map<String, Object> introspectionResult = mapper.readValue(resourceAsStream, new TypeReference<Map<String, Object>>() {});

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                                                          () -> UserDetailsConverter.convert(introspectionResult));
        assertEquals("Client name not found", exception.getMessage());
    }

    @Test
    @SneakyThrows
    public void convertSATJwtClaimsSuccess() {
        InputStream resourceAsStream = getClass().getResourceAsStream("/converter/jwt-sat-claims-success.json");
        Map<String, Object> introspectionResult = mapper.readValue(resourceAsStream, new TypeReference<Map<String, Object>>() {});

        AuthUserDetails result = UserDetailsConverter.convert(introspectionResult);

        assertEquals("10cJdfBOdv7TJlO4aw7ukfsiQ3YNEWmS", result.getId());
        assertEquals("MySa - Service Account", result.getUsername());
        assertEquals(new HashSet<>(Arrays.asList("AUDIT_LOGS_VIEW", "TMC_GROUP_MANAGEMENT", "TMC_USER_MANAGEMENT")),
                result.getEntitlements());
        assertEquals("7beeb6a2-f250-4c87-9178-facec3350bdd", result.getTenantId());
        assertEquals("jhervy.talend.com", result.getTenantName());
    }
}
