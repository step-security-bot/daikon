package org.talend.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * <p>
 * Disables Actuator security to ease sample application usage.
 * </p>
 */
@Configuration
public class SecurityConfiguration {

    @Bean("securityFilterChain.disablesActuatorSecurity")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Purpose is not showcase security, allow all accesses
        http.authorizeRequests().antMatchers("/**").permitAll();

        // Disable CSRF to ease demo with POSTs requests
        http.cors().and().csrf().disable();

        return http.build();
    }
}
