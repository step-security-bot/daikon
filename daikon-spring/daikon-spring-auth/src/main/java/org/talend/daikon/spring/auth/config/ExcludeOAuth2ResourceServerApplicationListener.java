package org.talend.daikon.spring.auth.config;

import java.util.*;

import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

public class ExcludeOAuth2ResourceServerApplicationListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    // @formatter:off
    public static final String EXCLUDE_PROPERTY = "spring.autoconfigure.exclude";
    public static final String AUTO_CONFIG_TO_EXCLUDE = OAuth2ResourceServerAutoConfiguration.class.getCanonicalName();

    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();

        Set<String> existingExclusionsValues = Optional.ofNullable(environment.getProperty(EXCLUDE_PROPERTY))
                .map(value -> value.split(","))
                .map(Arrays::asList)
                .map(HashSet::new)
                .orElse(new HashSet<String>());

        // read values coming from array, like spring.autoconfigure.exclude[0]
        for (int i = 0; environment.getProperty(EXCLUDE_PROPERTY + "[" + i + "]") != null; i++) {
            existingExclusionsValues.add(environment.getProperty(EXCLUDE_PROPERTY + "[" + i + "]"));
        }

        existingExclusionsValues.add(AUTO_CONFIG_TO_EXCLUDE);

        Properties updatedExclusionsProperties = new Properties();
        updatedExclusionsProperties.put(EXCLUDE_PROPERTY, String.join(",", existingExclusionsValues));
        environment.getPropertySources().addFirst(new PropertiesPropertySource("updatedExclusions", updatedExclusionsProperties));
    }
    // @formatter:on
}
