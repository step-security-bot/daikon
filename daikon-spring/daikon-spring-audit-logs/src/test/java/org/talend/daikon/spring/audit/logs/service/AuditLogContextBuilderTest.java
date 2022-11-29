package org.talend.daikon.spring.audit.logs.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.talend.daikon.spring.audit.common.exception.AuditLogException;
import org.talend.logging.audit.Context;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class AuditLogContextBuilderTest {

    @Test
    public void testJsonDateTimeSerialization() {

        Context build = AuditLogContextBuilder.create() //
                .withRequestId(UUID.randomUUID()) //
                .withTimestamp("timestamp") //
                .withAccountId(UUID.randomUUID().toString()) //
                .withLogId(UUID.randomUUID()) //
                .withApplicationId("appid") //
                .withEventType("eventtype") //
                .withEventCategory("category") //
                .withEventOperation("operation") //
                .withClientIp("ip") //
                .withRequest(Mockito.mock(HttpServletRequest.class), new TestEntity("one", 1, Instant.now())) //
                .withResponseBody(new TestEntity("one", 1, Instant.now())) //
                .withRequestUrl("url") //
                .withRequestMethod("GET") //
                .withResponseCode(200) //
                .build();

        assertNotNull(build);
    }

    @Data
    @AllArgsConstructor
    static class TestEntity {

        private String property;

        private int value;

        private Instant time;
    }

    @Test
    public void testValidateContextFieldsInvalidAccountId() {

        AuditLogContextBuilder contextBuilder = AuditLogContextBuilder.create().withRequestId(UUID.randomUUID())
                .withAccountId(UUID.randomUUID().toString()).withTimestamp("timestamp").withUserId("userId")
                .withLogId(UUID.randomUUID()).withApplicationId("appid").withEventType("eventtype").withEventCategory("category")
                .withEventOperation("operation").withClientIp("ip").withRequestUrl("url").withRequestMethod("GET")
                .withResponseCode(200);
        contextBuilder.getContext().put("request", "request");
        contextBuilder.getContext().put("response", "response");

        contextBuilder.withAccountId("null");
        AuditLogException ex = Assertions.assertThrows(AuditLogException.class, contextBuilder::checkAuditContextIsValid);
        assertEquals("UNEXPECTED_EXCEPTION:{message=audit log context has invalid UUID values: [null]}", ex.getMessage());

        contextBuilder.getContext().put("accountId", "accountId");
        ex = Assertions.assertThrows(AuditLogException.class, contextBuilder::checkAuditContextIsValid);
        assertEquals("UNEXPECTED_EXCEPTION:{message=audit log context has invalid UUID values: [accountId]}", ex.getMessage());

        contextBuilder.getContext().put("accountId", UUID.randomUUID().toString());
        contextBuilder.getRequest().put("accountId", "accountId-1");
        ex = Assertions.assertThrows(AuditLogException.class, contextBuilder::checkAuditContextIsValid);
        assertEquals("UNEXPECTED_EXCEPTION:{message=audit log context has invalid UUID values: [accountId-1]}", ex.getMessage());

        contextBuilder.getContext().put("accountId", UUID.randomUUID().toString());
        contextBuilder.getResponse().put("accountId", UUID.randomUUID().toString());
        contextBuilder.getRequest().put("accountId", UUID.randomUUID().toString());
        contextBuilder.withAccountId(UUID.randomUUID().toString());
        Assertions.assertDoesNotThrow(contextBuilder::checkAuditContextIsValid);

        contextBuilder.getResponse().put("accountId", null);
        contextBuilder.getRequest().put("accountId", null);
        contextBuilder.withAccountId(UUID.randomUUID().toString());
        Assertions.assertDoesNotThrow(contextBuilder::checkAuditContextIsValid);
    }

    @Test
    public void testValidateContextFieldsAnyUserIdAllowed() { // userId is allowed to be random string for SAT

        AuditLogContextBuilder contextBuilder = AuditLogContextBuilder.create().withRequestId(UUID.randomUUID())
                .withAccountId(UUID.randomUUID().toString()).withTimestamp("timestamp").withUserId("userId")
                .withLogId(UUID.randomUUID()).withApplicationId("appid").withEventType("eventtype").withEventCategory("category")
                .withEventOperation("operation").withClientIp("ip").withRequestUrl("url").withRequestMethod("GET")
                .withResponseCode(200);
        contextBuilder.getContext().put("request", "request");
        contextBuilder.getContext().put("response", "response");

        Assertions.assertDoesNotThrow(contextBuilder::checkAuditContextIsValid);

        contextBuilder.withUserId(UUID.randomUUID().toString());
        contextBuilder.getRequest().put("userId", UUID.randomUUID().toString());
        contextBuilder.getResponse().put("userId", UUID.randomUUID().toString());
        Assertions.assertDoesNotThrow(contextBuilder::checkAuditContextIsValid);

        contextBuilder.withUserId("userId");
        contextBuilder.getRequest().put("userId", "userId");
        contextBuilder.getResponse().put("userId", "userId");
        Assertions.assertDoesNotThrow(contextBuilder::checkAuditContextIsValid);

        contextBuilder.withUserId(null);
        contextBuilder.getRequest().put("userId", null);
        contextBuilder.getResponse().put("userId", null);
        Assertions.assertDoesNotThrow(contextBuilder::checkAuditContextIsValid);
    }

}
