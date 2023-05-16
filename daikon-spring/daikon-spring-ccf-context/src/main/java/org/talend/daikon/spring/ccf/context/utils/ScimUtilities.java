package org.talend.daikon.spring.ccf.context.utils;

import static java.lang.String.format;

import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.talend.daikon.spring.ccf.context.exception.CcfContextError;
import org.talend.iam.im.scim.client.UserClient;
import org.talend.iam.scim.exception.SCIMException;
import org.talend.iam.scim.model.SearchRequest;
import org.talend.iam.scim.model.SearchResponse;
import org.talend.iam.scim.model.User;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ScimUtilities {

    private final UserClient userClient;

    public ScimUtilities(UserClient userClient) {
        this.userClient = userClient;
    }

    @Cacheable(cacheNames = "${spring.ccf.context.cache.name:ccfScimCache}")
    public User getUserWithAttributes(String userId, List<String> parameterList) {
        User result = null;
        try {
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setAttributes(Set.copyOf(parameterList));
            searchRequest.setFilter(format("id eq \"%s\"", userId));
            SearchResponse<User> scimUserResponse = userClient.find(searchRequest);
            if (scimUserResponse.getResources().isEmpty()) {
                log.warn("SCIM user not found by userId - {}", userId);
            } else {
                result = scimUserResponse.getResources().get(0);
            }
        } catch (SCIMException | ResourceAccessException e) {
            log.warn("error while accessing SCIM", e);
            throw new CcfContextError("error while accessing SCIM");
        }
        return result;
    }
}
