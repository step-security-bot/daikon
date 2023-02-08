package org.talend.daikon.spring.reactive.sat.authentication;

import com.nimbusds.jose.shaded.json.JSONArray;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TalendJwtConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    public static final String TALEND_ENTITLEMENTS_FIELD = "entitlements";

    public static final String TALEND_PERMISSIONS_FIELD = "permissions";

    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<String> ents = Arrays.asList(((String) jwt.getClaims().getOrDefault(TALEND_ENTITLEMENTS_FIELD, "")).split(","));
        List<String> permsList = ((JSONArray) jwt.getClaims().getOrDefault(TALEND_PERMISSIONS_FIELD, new JSONArray())).stream()
                .map(Object::toString).collect(Collectors.toList());
        List<String> authorities = Stream.concat(permsList.stream(), ents.stream()).filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        return authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}
