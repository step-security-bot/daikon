package org.talend.daikon.spring.audit.logs.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.talend.daikon.spring.audit.logs.exception.AuditLogException;
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
    public void testValidateContextFields() {

        AuditLogContextBuilder contextBuilder = AuditLogContextBuilder.create().withRequestId(UUID.randomUUID())
                .withAccountId(UUID.randomUUID().toString()).withTimestamp("timestamp").withUserId("userId")
                .withLogId(UUID.randomUUID()).withApplicationId("appid").withEventType("eventtype").withEventCategory("category")
                .withEventOperation("operation").withClientIp("ip").withRequestUrl("url").withRequestMethod("GET")
                .withResponseCode(200);
        contextBuilder.getContext().put("request", "request");
        contextBuilder.getContext().put("response", "response");

        Assertions.assertThrows(AuditLogException.class, () -> contextBuilder.checkAuditContextIsValid());

        contextBuilder.withAccountId("null");
        contextBuilder.withUserId(UUID.randomUUID().toString());

        Assertions.assertThrows(AuditLogException.class, () -> contextBuilder.checkAuditContextIsValid());

        contextBuilder.withAccountId(UUID.randomUUID().toString());

        contextBuilder.checkAuditContextIsValid();

        contextBuilder.getContext().put("accountId", "accountId");

        Assertions.assertThrows(AuditLogException.class, () -> contextBuilder.checkAuditContextIsValid());

        contextBuilder.getContext().put("accountId", UUID.randomUUID().toString());
        contextBuilder.getRequest().put("userId", "userId");

        Assertions.assertThrows(AuditLogException.class, () -> contextBuilder.checkAuditContextIsValid());

        contextBuilder.getRequest().put("userId", UUID.randomUUID().toString());
        contextBuilder.getResponse().put("userId", "userId");

        Assertions.assertThrows(AuditLogException.class, () -> contextBuilder.checkAuditContextIsValid());

        contextBuilder.getResponse().put("userId", UUID.randomUUID().toString());

        contextBuilder.checkAuditContextIsValid();

        contextBuilder.getRequest().put("userId", null);

        contextBuilder.checkAuditContextIsValid();
    }

}
