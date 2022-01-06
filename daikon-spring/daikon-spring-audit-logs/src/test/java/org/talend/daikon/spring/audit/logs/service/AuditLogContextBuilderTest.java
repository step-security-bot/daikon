package org.talend.daikon.spring.audit.logs.service;

import static org.junit.Assert.assertNotNull;

import java.time.Instant;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.talend.logging.audit.Context;

import lombok.AllArgsConstructor;
import lombok.Data;

@RunWith(MockitoJUnitRunner.class)
public class AuditLogContextBuilderTest {

    @Test
    public void testJsonDateTimeSerialization() {

        Context build = AuditLogContextBuilder.create() //
                .withRequestId(UUID.randomUUID()) //
                .withTimestamp("timestamp") //
                .withAccountId("accountId") //
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

}
