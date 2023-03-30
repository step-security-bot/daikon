package org.talend.daikon.spring.auth.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.talend.daikon.spring.auth.exception.ExceptionHandlingTestApplication;

public class ExclusionsConfigTest {

    public static final String EXCLUDE_PROPERTY = "spring.autoconfigure.exclude";

    @Nested
    @SpringBootTest(classes = {
            ExceptionHandlingTestApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @ActiveProfiles("yaml-list")
    @ExtendWith(SpringExtension.class)
    public class YamlListTest {

        @Autowired
        private Environment environment;

        @Test
        public void testYamlList() {
            assertTrue(environment.containsProperty(EXCLUDE_PROPERTY));
            assertEquals("org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",
                    environment.getProperty(EXCLUDE_PROPERTY));
        }
    }

    @Nested
    @SpringBootTest(classes = {
            ExceptionHandlingTestApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
    @ActiveProfiles("comma-separated")
    @ExtendWith(SpringExtension.class)
    public class CommaSeparatedListTest {

        @Autowired
        private Environment environment;

        @Test
        public void testCommaSeparated() {
            assertTrue(environment.containsProperty(EXCLUDE_PROPERTY));
            assertEquals("org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration,"
                    + "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",
                    environment.getProperty(EXCLUDE_PROPERTY));
        }
    }

}
