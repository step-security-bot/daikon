package org.talend.daikon.spring.auth.common.model.userdetails;

import static org.talend.daikon.spring.auth.common.model.userdetails.JwtClaims.*;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

/**
 * Converts jwt claims or PAT introspection result into {@link AuthUserDetails} object
 * with {@link UserDetailsConverter#convert(Map)} method
 */
public class UserDetailsConverter {

    private static final Logger LOG = LoggerFactory.getLogger(UserDetailsConverter.class);

    private static final List<String> USERNAME_CLAIMS = Arrays.asList("login", "username", "preferred_username");

    private static Map<String, BiConsumer<AuthUserDetails, Object>> claimToPropertySetter = new HashMap<>();
    static {
        claimToPropertySetter.put(SUBJECT_CLAIM, (user, id) -> user.setId((String) id));
        claimToPropertySetter.put(EMAIL_CLAIM, (user, email) -> user.setEmail((String) email));

        claimToPropertySetter.put(GIVEN_NAME_CLAIM, (user, firstName) -> user.setFirstName((String) firstName));
        claimToPropertySetter.put(FAMILY_NAME_CLAIM, (user, lastName) -> user.setLastName((String) lastName));
        claimToPropertySetter.put(NAME_CLAIM, (user, name) -> user.setName((String) name));

        claimToPropertySetter.put(TENANT_ID_CLAIM, (user, tenantId) -> user.setTenantId((String) tenantId));
        claimToPropertySetter.put(TENANT_NAME_CLAIM, (user, tenantName) -> user.setTenantName((String) tenantName));

        claimToPropertySetter.put(PREFERRED_LANGUAGE_CLAIM, preferredLanguageSetter());
        claimToPropertySetter.put(TIMEZONE_CLAIM, (user, timezone) -> user.setTimezone((String) timezone));

        claimToPropertySetter.put(PENDO_COMPANY_NAME_CLAIM, pendoCompanyNameSetter());
        claimToPropertySetter.put(PENDO_USER_ID_CLAIM, (user, pendoUserId) -> user.setPendoUserId((String) pendoUserId));
        claimToPropertySetter.put(PENDO_DATACENTER_CLAIM, (user, dataCenter) -> user.setPendoDataCenter((String) dataCenter));

        claimToPropertySetter.put(SUBSCRIPTION_TYPE, subscriptionTypeSetter());
        claimToPropertySetter.put(SALES_FORCE_CONTACT_ID, salesForceContactIdSetter());
        claimToPropertySetter.put(SALES_FORCE_ACCOUNT_ID, salesForceAccountIdSetter());
        claimToPropertySetter.put(IPC_ENABLED, (user, ipcEnabled) -> user.setIpcEnabled((Boolean) ipcEnabled));

        claimToPropertySetter.put(APPLICATIONS_CLAIM, applicationsSetter());
        claimToPropertySetter.put(GROUP_IDS_CLAIM, groupIdsSetter());
    }

    public static AuthUserDetails convert(Map<String, Object> jwtClaims) {
        AuthUserDetails authUserDetails = new AuthUserDetails(extractUserName(jwtClaims), "N/A", extractAuthorities(jwtClaims));
        authUserDetails.setAttributes(jwtClaims);
        setProperties(authUserDetails, jwtClaims);

        return authUserDetails;
    }

    private static String extractUserName(Map<String, Object> jwtClaims) {
        return USERNAME_CLAIMS.stream().filter(claim -> jwtClaims.get(claim) != null)
                .map(claim -> jwtClaims.get(claim).toString()).findFirst().orElseThrow(() -> {
                    LOG.warn("Cannot create AuthUserDetails without username: {}", jwtClaims);
                    return new IllegalArgumentException("Username not found");
                });
    }

    private static Collection<GrantedAuthority> extractAuthorities(Map<String, Object> attributes) {
        String entitlements = (String) attributes.get(JwtClaims.ENTITLEMENTS_CLAIM);
        return Arrays.stream(entitlements.split(",")).map(String::trim).filter(StringUtils::hasText)
                .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    private static void setProperties(AuthUserDetails authUserDetails, Map<String, Object> jwtClaims) {
        for (Map.Entry<String, Object> claimAndValue : jwtClaims.entrySet()) {
            BiConsumer<AuthUserDetails, Object> setter = claimToPropertySetter.get(claimAndValue.getKey());
            if (null != setter) {
                setter.accept(authUserDetails, claimAndValue.getValue());
            } else {
                LOG.debug("No setter found for claim '{}'", claimAndValue.getKey());
            }
        }
    }

    private static BiConsumer<AuthUserDetails, Object> groupIdsSetter() {
        return (user, groupIds) -> user.setGroupIds((Collection<String>) groupIds);
    }

    private static BiConsumer<AuthUserDetails, Object> applicationsSetter() {
        return (user, applications) -> user.setApplications(new HashSet<>((Collection<String>) applications));
    }

    private static BiConsumer<AuthUserDetails, Object> salesForceAccountIdSetter() {
        return (user, salesForceAccountId) -> user.setSalesForceAccountId((String) salesForceAccountId);
    }

    private static BiConsumer<AuthUserDetails, Object> salesForceContactIdSetter() {
        return (user, salesForceContactId) -> user.setSalesForceContactId((String) salesForceContactId);
    }

    private static BiConsumer<AuthUserDetails, Object> subscriptionTypeSetter() {
        return (user, subscriptionType) -> user.setSubscriptionType((String) subscriptionType);
    }

    private static BiConsumer<AuthUserDetails, Object> pendoCompanyNameSetter() {
        return (user, companyName) -> user.setPendoCompanyName((String) companyName);
    }

    private static BiConsumer<AuthUserDetails, Object> preferredLanguageSetter() {
        return (user, preferredLanguage) -> user.setPreferredLanguage((String) preferredLanguage);
    }
}
