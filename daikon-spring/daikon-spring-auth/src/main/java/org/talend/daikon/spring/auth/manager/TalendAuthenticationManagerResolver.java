package org.talend.daikon.spring.auth.manager;

import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_REQUEST;
import static org.talend.daikon.spring.auth.manager.AuthenticationManagerFactory.auth0JwtAuthenticationManager;
import static org.talend.daikon.spring.auth.manager.AuthenticationManagerFactory.iamJwtAuthenticationManager;
import static org.talend.daikon.spring.auth.manager.AuthenticationManagerFactory.opaqueTokenAuthenticationManager;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.cache.Cache;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.util.StringUtils;
import org.talend.daikon.spring.auth.provider.Auth0AuthenticationProvider;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

/**
 * Return authentication manager based on the Bearer token provided in Authorization header
 */
public class TalendAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TalendAuthenticationManagerResolver.class);

    private final OAuth2ResourceServerProperties iamOauth2Properties;

    private final OAuth2ResourceServerProperties auth0Oauth2Properties;

    private final List<Auth0AuthenticationProvider> auth0AuthenticationProviders;

    private final AuthenticationManager iamJwtAuthenticationManager;

    private final AuthenticationManager opaqueTokenAuthenticationManager;

    private final AuthenticationManager auth0JwtAuthenticationManager;

    public TalendAuthenticationManagerResolver(OAuth2ResourceServerProperties iamOauth2Properties,
            OAuth2ResourceServerProperties auth0Oauth2Properties, List<Auth0AuthenticationProvider> auth0AuthenticationProviders,
            Cache jwkSetCache) {
        this.iamOauth2Properties = iamOauth2Properties;
        this.auth0Oauth2Properties = auth0Oauth2Properties;
        this.auth0AuthenticationProviders = auth0AuthenticationProviders;

        this.auth0JwtAuthenticationManager = auth0JwtAuthenticationManager(auth0Oauth2Properties, auth0AuthenticationProviders,
                jwkSetCache);
        this.iamJwtAuthenticationManager = iamJwtAuthenticationManager(iamOauth2Properties, jwkSetCache);
        this.opaqueTokenAuthenticationManager = opaqueTokenAuthenticationManager(iamOauth2Properties);

    }

    @Override
    public AuthenticationManager resolve(HttpServletRequest request) {
        LOGGER.debug("Resolve authentication manager");

        String token = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .map(header -> header.replace("Bearer ", ""))
                .orElseThrow(() -> new OAuth2AuthenticationException(new OAuth2Error(INVALID_REQUEST)));

        try {
            JWT jwt = JWTParser.parse(token);
            String issuer = jwt.getJWTClaimsSet().getIssuer();

            if (StringUtils.hasText(issuer) && issuer.equals(auth0Oauth2Properties.getJwt().getIssuerUri())) {
                LOGGER.debug("JWT is issued by Auth0 '{}': trying to authenticate with auth0Jwt AuthenticationManager", issuer);
                return auth0JwtAuthenticationManager;
            }

            LOGGER.debug("JWT issuer is '{}': trying to authenticate with default iamJwt AuthenticationManager", issuer);
            return iamJwtAuthenticationManager;
        } catch (ParseException e) {
            LOGGER.debug("Cannot parse the token as JWT: trying to authenticate with opaqueToken AuthenticationManager");
            return opaqueTokenAuthenticationManager;
        } catch (Exception ex) {
            LOGGER.error("Unexpected exception during auth manager resolving process: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

}
