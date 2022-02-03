package org.talend.daikon.spring.reactive.sat.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ServerWebExchange;
import org.talend.daikon.spring.reactive.sat.authentication.Auth0ReactiveAuthenticationProvider;
import org.talend.daikon.spring.reactive.sat.authentication.SatReactiveAuthenticationProvider;
import org.talend.daikon.spring.reactive.sat.authentication.TalendReactiveAuthenticationManagerResolver;

/**
 * Enables Service Account Token authentication
 */
@Configuration
@ConditionalOnClass(value = { Jwt.class, OAuth2ResourceServerProperties.class })
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class TalendReactiveAuthenticationAutoConfiguration {

    @Bean
    @Qualifier("satReactiveAuthenticationProvider")
    public Auth0ReactiveAuthenticationProvider satReactiveAuthenticationProvider() {
        return new SatReactiveAuthenticationProvider();
    }

    @Bean
    @Qualifier("reactiveAuthenticationManagerFactory")
    @ConditionalOnBean(OAuth2ResourceServerProperties.class)
    @ConditionalOnMissingBean(ReactiveAuthenticationManagerFactory.class)
    public ReactiveAuthenticationManagerFactory reactiveAuthenticationManagerFactory(
            OAuth2ResourceServerProperties oauth2Properties) {
        return new ReactiveAuthenticationManagerFactory(oauth2Properties, Arrays.asList(satReactiveAuthenticationProvider()));
    }

    @Bean
    @Qualifier("talendReactiveAuthenticationManagerResolver")
    @ConditionalOnBean(OAuth2ResourceServerProperties.class)
    @ConditionalOnMissingBean(name = "talendReactiveAuthenticationManagerResolver")
    public ReactiveAuthenticationManagerResolver<ServerWebExchange> talendReactiveAuthenticationManagerResolver(
            OAuth2ResourceServerProperties oauth2Properties) {
        return new TalendReactiveAuthenticationManagerResolver(oauth2Properties,
                reactiveAuthenticationManagerFactory(oauth2Properties));
    }

}
