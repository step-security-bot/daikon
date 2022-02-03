package org.talend.daikon.spring.auth.common.model.userdetails;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.fasterxml.jackson.annotation.*;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * This userDetails contains a little more information than classic User object like firstName, lastName, emails, groups
 *
 * @author agonzalez
 */
@JsonInclude(NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true, value = { "entitlements" }, allowGetters = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class AuthUserDetails extends User implements AuthIdProvider {

    private static final long serialVersionUID = 2055118616753764874L;

    private static final String ATTR_USERNAME = "username";

    private static final String ATTR_ENABLED = "enabled";

    private static final String ATTR_ACCOUNT_NON_EXPIRED = "nonExpired";

    private static final String ATTR_CREDENTIALS_NON_EXPIRED = "credentialsNonExpired";

    private static final String ATTR_ACCOUNT_NON_LOCKED = "nonLocked";

    private static final String ATTR_PREFERRED_LANGUAGE = "preferredLanguage";

    private static final String ATTR_AUTHORITIES = "authorities";

    private String id;

    private String firstName;

    private String lastName;

    private String name;

    private String email;

    private String preferredLanguage;

    private String timezone;

    private Set<String> applications = new HashSet<>();

    private Collection<String> groupIds = new ArrayList<>();

    private String tenantId;

    private String tenantName;

    private String pendoUserId;

    private String pendoCompanyName;

    private String pendoDataCenter;

    private String subscriptionType;

    private String salesForceContactId;

    private String salesForceAccountId;

    private Boolean ipcEnabled;

    public AuthUserDetails(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    @JsonCreator
    public AuthUserDetails(@JsonProperty(ATTR_USERNAME) String username, @JsonProperty(ATTR_ENABLED) boolean enabled,
            @JsonProperty(ATTR_ACCOUNT_NON_EXPIRED) boolean accountNonExpired,
            @JsonProperty(ATTR_CREDENTIALS_NON_EXPIRED) boolean credentialsNonExpired,
            @JsonProperty(ATTR_ACCOUNT_NON_LOCKED) boolean accountNonLocked,
            @JsonProperty(ATTR_PREFERRED_LANGUAGE) String preferredLanguage,
            @JsonProperty(ATTR_AUTHORITIES) Collection<SimpleGrantedAuthority> authorities) {
        this(username, "N/A", enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, preferredLanguage,
                authorities != null ? authorities : new ArrayList<GrantedAuthority>());
    }

    public AuthUserDetails(String username, String password, boolean enabled, boolean accountNonExpired,
            boolean credentialsNonExpired, boolean accountNonLocked, String preferredLanguage,
            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.preferredLanguage = preferredLanguage;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return super.getPassword();
    }

    public void addGroupId(String group) {
        this.groupIds.add(group);
    }

    public Set<String> getEntitlements() {
        return AuthorityUtils.authorityListToSet(getAuthorities());
    }

    public void setGroupIds(Collection<String> groupIds) {
        if (groupIds != null) {
            this.groupIds = groupIds;
        } else {
            this.groupIds = new ArrayList<>();
        }
    }

    public void setApplications(Set<String> applications) {
        if (applications != null) {
            this.applications = applications;
        } else {
            this.applications = new HashSet<>();
        }
    }

    @Override
    public String toString() {
        return getUsername();
    }
}
