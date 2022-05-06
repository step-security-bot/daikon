package org.talend.daikon.spring.auth.manager;

import static org.springframework.security.oauth2.core.OAuth2ErrorCodes.INVALID_REQUEST;

import java.text.ParseException;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.util.StringUtils;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;

import lombok.Builder;

/**
 * Return authentication manager based on the Bearer token provided in Authorization header
 */
@Builder
public class TalendAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TalendAuthenticationManagerResolver.class);

    private final String auth0IssuerUri;

    private final AuthenticationManager iamJwtAuthenticationManager;

    private final AuthenticationManager opaqueTokenAuthenticationManager;

    private final AuthenticationManager auth0JwtAuthenticationManager;

    @Override
    public AuthenticationManager resolve(HttpServletRequest request) {
        LOGGER.debug("Resolve authentication manager");

        String token = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .map(header -> header.replace("Bearer ", ""))
                .orElseThrow(() -> new OAuth2AuthenticationException(new OAuth2Error(INVALID_REQUEST)));

        try {
            JWT jwt = JWTParser.parse(token);
            String issuer = jwt.getJWTClaimsSet().getIssuer();

            if (StringUtils.hasText(issuer) && issuer.equals(auth0IssuerUri)) {
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
