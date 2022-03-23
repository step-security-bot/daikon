package org.talend.daikon.spring.auth.common.model.userdetails;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

public class UserDetailsConverterTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    @SneakyThrows
    public void convertPatIntrospectionSuccess() {
        InputStream resourceAsStream = getClass().getResourceAsStream("/converter/pat-introspection-success.json");
        Map<String, Object> introspectionResult = mapper.readValue(resourceAsStream, new TypeReference<Map<String, Object>>() {});

        AuthUserDetails result = UserDetailsConverter.convert(introspectionResult);

        Assert.assertEquals("befb746d-10b2-442e-974c-896d26379573", result.getId());
        Assert.assertEquals("mbutko@mbutko01.us.talend.com", result.getUsername());
        Assert.assertEquals("User Talend", result.getName());
        Assert.assertEquals("user@yopmails.com", result.getEmail());
        Assert.assertEquals("9133741e-d49d-4cd8-a09e-9791fead2583", result.getTenantId());
        Assert.assertEquals("mbutko01.us.talend.com", result.getTenantName());
        Assert.assertEquals(new HashSet<>(Arrays.asList("CRAWLING_CREATE", "TMC_OPERATOR", "DATASET_READ")),
                            result.getEntitlements());
        Assert.assertEquals(Arrays.asList("48e38a59-ce6f-497a-84b9-63598ff8304b", "914413da-6a86-4a5f-b753-a963d1916179"),
                            result.getGroupIds());
    }

    @Test
    @SneakyThrows
    public void convertJwtClaimsSuccess() {
        InputStream resourceAsStream = getClass().getResourceAsStream("/converter/jwt-claims-success.json");
        Map<String, Object> introspectionResult = mapper.readValue(resourceAsStream, new TypeReference<Map<String, Object>>() {});

        AuthUserDetails result = UserDetailsConverter.convert(introspectionResult);

        Assert.assertEquals("8e19e56b-4844-454b-8d0f-17d4be7f057e", result.getId());
        Assert.assertEquals("user.mbutko.1.yopmail.com@mbutko01.us.talend.com", result.getUsername());
        Assert.assertEquals("user test", result.getName());
        Assert.assertEquals("user.email.ut@yopmail.com", result.getEmail());
        Assert.assertEquals("9133741e-d49d-4cd8-a09e-9791fead2583", result.getTenantId());
        Assert.assertEquals("mbutko01.us.talend.com", result.getTenantName());
        Assert.assertTrue(result.getEntitlements().isEmpty());
        Assert.assertTrue(result.getGroupIds().isEmpty());
    }

    @Test
    @SneakyThrows
    public void convertPatIntrospectionWithoutUsernameClaimsFail() {
        InputStream resourceAsStream = getClass().getResourceAsStream("/converter/pat-introspection-fail.json");
        Map<String, Object> introspectionResult = mapper.readValue(resourceAsStream, new TypeReference<Map<String, Object>>() {});

        Assert.assertThrows(IllegalArgumentException.class, () -> UserDetailsConverter.convert(introspectionResult));
    }
}
