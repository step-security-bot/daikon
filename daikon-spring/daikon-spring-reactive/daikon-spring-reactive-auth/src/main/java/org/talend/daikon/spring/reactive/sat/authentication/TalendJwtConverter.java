package org.talend.daikon.spring.reactive.sat.authentication;

import static ch.qos.logback.core.CoreConstants.EMPTY_STRING;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class TalendJwtConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String TALEND_SCOPE_FIELD = "entitlements";

    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<String> authorities = Arrays
                .asList(((String) jwt.getClaims().getOrDefault(TALEND_SCOPE_FIELD, EMPTY_STRING)).split(","));
        return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}
