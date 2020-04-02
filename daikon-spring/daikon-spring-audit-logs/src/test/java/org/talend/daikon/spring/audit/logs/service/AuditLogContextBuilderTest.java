package org.talend.daikon.spring.audit.logs.service;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.talend.daikon.spring.audit.logs.service.AuditLogContextBuilder;
import org.talend.logging.audit.Context;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AuditLogContextBuilderTest {

    @Test
    public void testWithRequest() {
        HttpStatus httpStatus = HttpStatus.ACCEPTED;
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setRemoteHost("localhost");
        request.setRemotePort(80);
        request.setRequestURI("/");
        request.setMethod("POST");
        request.addHeader("User-Agent", "user agent");

        String fakeUsername = "fakeUser";
        String accountId = "accountId";
        String userId = "userId";
        String email = "fakeUser@talend.com";

        String applicationId = "TTT";
        String eventType = "a type";
        String eventCategory = "a category";
        String eventOperation = "an operation";
        String timestamp = OffsetDateTime.now().toString();
        Context context = AuditLogContextBuilder.create().withTimestamp(timestamp).withLogId(UUID.randomUUID())
                .withRequestId(UUID.randomUUID()).withApplicationId(applicationId).withEventType(eventType)
                .withEventOperation(eventOperation).withEventCategory(eventCategory).withUserId(userId).withUsername(fakeUsername)
                .withEmail(email).withAccountId(accountId).withRequest(request).withResponse(httpStatus.value(), null).build();

        assertNotNull(context.get("timestamp"));
        assertNotNull(context.get("log_id"));
        assertNotNull(context.get("request_id"));
        assertEquals("accountId", context.get("account_id"));
        assertEquals("userId", context.get("user_id"));
        assertEquals("fakeUser", context.get("username"));
        assertEquals("TTT", context.get("application_id"));
        assertEquals("a type", context.get("event_type"));
        assertEquals("a category", context.get("event_category"));
        assertEquals("an operation", context.get("event_operation"));
        assertEquals("127.0.0.1", context.get("client_ip"));
        assertEquals("{\"url\":\"http://localhost/\",\"method\":\"POST\",\"user_agent\":\"user agent\"}", context.get("request"));
        assertEquals("{\"code\":\"202\"}", context.get("response"));

    }
}