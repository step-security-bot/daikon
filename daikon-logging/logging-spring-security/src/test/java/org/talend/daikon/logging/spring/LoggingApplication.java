// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.daikon.logging.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;

@SpringBootApplication
public class LoggingApplication {

    public static final String MESSAGE = "Hello, World!";

    public static final String USER_ID = "user";

    public static final String PASSWORD = "password";

    public static void main(String[] args) { // NOSONAR
        SpringApplication.run(LoggingApplication.class, args); // NOSONAR
    }

    @Configuration
    public class CustomSecurityConfiguration {

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public WebSecurityCustomizer webSecurityCustomizer() {
            return (web) -> web.ignoring().antMatchers("/public/**");
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf().disable().authorizeRequests().anyRequest().authenticated().and().httpBasic();
            return http.build();
        }

        @Bean
        public UserDetailsManager users(PasswordEncoder passwordEncoder) {
            String password = passwordEncoder.encode(PASSWORD);
            UserDetails user = User.builder().username(USER_ID).password(password).authorities("ROLE_USER").build();
            return new InMemoryUserDetailsManager(user);
        }
    }

    @RestController
    public static class SampleEndpoint {

        private final SampleRequestHandler sampleRequestHandler;

        public SampleEndpoint(SampleRequestHandler sampleRequestHandler) {
            this.sampleRequestHandler = sampleRequestHandler;
        }

        @RequestMapping
        public String sampleGet() {
            this.sampleRequestHandler.onSampleRequestCalled();
            return MESSAGE;
        }

        @RequestMapping(path = "/public")
        public String publicSampleGet() {
            this.sampleRequestHandler.onSampleRequestCalled();
            return MESSAGE;
        }

        @RequestMapping(path = "/async")
        public Callable<String> asyncGet() {
            return () -> {
                this.sampleRequestHandler.onSampleRequestCalled();
                return MESSAGE;
            };
        }

        @RequestMapping(path = "/public/async")
        public Callable<String> publicAsyncGet() {
            return () -> {
                this.sampleRequestHandler.onSampleRequestCalled();
                return MESSAGE;
            };
        }
    }

    public interface SampleRequestHandler {

        void onSampleRequestCalled();

    }

}
