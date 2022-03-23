package org.talend.logging.audit.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.talend.logging.audit.impl.AuditConfiguration.APPENDER_FILE_PATH;
import static org.talend.logging.audit.impl.AuditConfiguration.APPLICATION_NAME;
import static org.talend.logging.audit.impl.AuditConfiguration.LOG_APPENDER;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

public class AuditConfigurationMapImplTest {

    private Properties properties = new Properties();

    @BeforeEach
    public void setUp() {
        setProperty(LOG_APPENDER, "none");
        setProperty(APPENDER_FILE_PATH, "/tmp");
        setProperty(APPLICATION_NAME, "App name");
    }

    @Test
    public void validateConfigurationWithValideOne() {
        AuditConfigurationMap config = AuditConfiguration.loadFromProperties(properties);

        config.validateConfiguration();
    }

    @Test
    public void validateConfigurationNotRequiredFilePath() {
        setProperty(APPENDER_FILE_PATH, null);

        AuditConfigurationMap config = AuditConfiguration.loadFromProperties(properties);
        config.validateConfiguration();
    }

    @Test
    public void validateConfigurationRequiredFilePath() {
        assertThrows(IllegalArgumentException.class, () -> {
            setProperty(LOG_APPENDER, "file");
            setProperty(APPENDER_FILE_PATH, null);

            AuditConfigurationMap config = AuditConfiguration.loadFromProperties(properties);
            config.validateConfiguration();
        });
    }

    @Test
    public void toPropertyTest() {
        String property = toProperty(APPENDER_FILE_PATH);
        assertEquals("appender.file.path", property);
    }

    private void setProperty(AuditConfiguration config, String value) {
        String key = toProperty(config);
        if (value == null) {
            properties.remove(key);
        } else {
            properties.setProperty(key, value);
        }
    }

    private String toProperty(AuditConfiguration config) {
        return config.name().toLowerCase().replaceAll("_", ".");
    }

}