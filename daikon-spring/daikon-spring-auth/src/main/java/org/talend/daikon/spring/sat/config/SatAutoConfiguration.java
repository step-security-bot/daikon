package org.talend.daikon.spring.sat.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.talend.daikon.spring.sat.manager.Auth0AuthenticationManager;
import org.talend.daikon.spring.sat.provider.Auth0AuthenticationProvider;
import org.talend.daikon.spring.sat.provider.SatAuthenticationProvider;

import lombok.AllArgsConstructor;

/**
 * Enables Service Account Token authentication
 */
@Configuration
@ConditionalOnClass(value = { Jwt.class, OAuth2ResourceServerProperties.class })
public class SatAutoConfiguration {

    @Bean
    public Auth0AuthenticationProvider satAuthenticationProvider() {
        return new SatAuthenticationProvider();
    }

    @Bean
    @Qualifier("auth0AuthenticationManager")
    @ConditionalOnBean(ResourceServerTokenServices.class)
    @ConditionalOnMissingBean(Auth0AuthenticationManager.class)
    public Auth0AuthenticationManager auth0AuthenticationManager(OAuth2ResourceServerProperties oauth2Properties,
            ResourceServerTokenServices legacyTokenService, List<Auth0AuthenticationProvider> providers) {
        OAuth2AuthenticationManager delegate = new OAuth2AuthenticationManager();
        delegate.setTokenServices(legacyTokenService);
        return new Auth0AuthenticationManager(oauth2Properties, delegate, providers);
    }

    @Bean
    @ConditionalOnBean(Auth0AuthenticationManager.class)
    public ResourceServerConfigurer auth0ResourceServerConfigurer(Auth0AuthenticationManager auth0AuthenticationManager) {
        return new Auth0ResourceServerConfigurer(auth0AuthenticationManager);
    }

    @AllArgsConstructor
    public static class Auth0ResourceServerConfigurer implements ResourceServerConfigurer {

        private AuthenticationManager authenticationManager;

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.authenticationManager(authenticationManager);
        }

        @Override
        public void configure(HttpSecurity http) {
            // do nothing
        }

    }

}
