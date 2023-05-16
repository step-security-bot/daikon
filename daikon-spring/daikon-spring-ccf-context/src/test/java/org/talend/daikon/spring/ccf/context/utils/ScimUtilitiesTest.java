package org.talend.daikon.spring.ccf.context.utils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.talend.daikon.spring.ccf.context.UserContextConstant;
import org.talend.daikon.spring.ccf.context.exception.CcfContextError;
import org.talend.iam.im.scim.client.UserClient;
import org.talend.iam.scim.exception.SCIMException;
import org.talend.iam.scim.model.GroupRef;
import org.talend.iam.scim.model.SearchResponse;
import org.talend.iam.scim.model.User;

@ExtendWith(MockitoExtension.class)
class ScimUtilitiesTest {

    private static final String USER_ID = UUID.randomUUID().toString();

    @Mock
    private UserClient userClient;

    @InjectMocks
    private ScimUtilities scimUtilities;

    @Test
    void whenUserIdAndParameterReturnUser() {
        User returnedUser = new User.Builder().setId(USER_ID).setGroups(List.of(new GroupRef())).build();
        Mockito.when(userClient.find(Mockito.any())).thenReturn(new SearchResponse<User>(List.of(returnedUser), 1, 1, 0));
        User result = scimUtilities.getUserWithAttributes(USER_ID, List.of(UserContextConstant.GROUPS.getValue()));

        Assertions.assertEquals(returnedUser, result);
    }

    @Test
    void whenUserNotFoundReturnNull() {
        Mockito.when(userClient.find(Mockito.any())).thenReturn(new SearchResponse<User>(Collections.emptyList(), 0, 0, 0));
        User result = scimUtilities.getUserWithAttributes(USER_ID, List.of(UserContextConstant.GROUPS.getValue()));

        Assertions.assertNull(result);
    }

    @Test
    void whenScimErrorThrowException() {
        Mockito.when(userClient.find(Mockito.any())).thenThrow(new SCIMException("error"));
        List<String> userContextConstants = List.of(UserContextConstant.GROUPS.getValue());

        Assertions.assertThrows(CcfContextError.class, () -> scimUtilities.getUserWithAttributes(USER_ID, userContextConstants));
    }
}
