package org.talend.logging.audit.impl;

import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Proxy;

import org.junit.jupiter.api.Test;
import org.talend.logging.audit.AuditEvent;
import org.talend.logging.audit.AuditLoggerFactory;
import org.talend.logging.audit.Context;
import org.talend.logging.audit.ContextBuilder;
import org.talend.logging.audit.LogLevel;

public class ProxyEventAuditLoggerTest {

    @Test
    public void testEventAuditLogger() {
        Context ctx = ContextBuilder.emptyContext();
        Throwable thr = new IllegalStateException();

        AuditLoggerBase base = mock(AuditLoggerBase.class);
        base.log(LogLevel.WARNING, "testcat", ctx, thr, "testmsg");
        base.log(LogLevel.INFO, "testcat2", null, null, "testmsg2");
        replay(base);

        TestEvent testEvent = getEventAuditLogger(base);

        testEvent.testWithParams(thr, ctx);
        testEvent.testWithoutParams();

        verify(base);
    }

    @Test
    public void testTooManyArguments() {
        assertThrows(IllegalArgumentException.class, () -> {
            Context ctx = ContextBuilder.emptyContext();
            Throwable thr = new IllegalStateException();
            TestEvent testEvent = getEventAuditLogger(null);
            testEvent.testWithParams(ctx, thr, "");
        });
    }

    @Test
    public void testRepeatedContext() {
        assertThrows(IllegalArgumentException.class, () -> {
            Context ctx = ContextBuilder.emptyContext();
            TestEvent testEvent = getEventAuditLogger(null);
            testEvent.testWithParams(ctx, ctx);
        });
    }

    @Test
    public void testRepeatedThrowable() {
        assertThrows(IllegalArgumentException.class, () -> {
            Throwable thr = new IllegalStateException();
            TestEvent testEvent = getEventAuditLogger(null);
            testEvent.testWithParams(thr, thr);
        });
    }

    @Test
    public void testMissingAnnotation() {
        assertThrows(IllegalArgumentException.class, () -> {
            TestEvent testEvent = getEventAuditLogger(null);
            testEvent.notEvent();
        });
    }

    @Test
    public void testWrongArgumentType() {
        assertThrows(IllegalArgumentException.class, () -> {
            TestEvent testEvent = getEventAuditLogger(null);
            testEvent.testWithParams("");
        });
    }

    private static TestEvent getEventAuditLogger(AuditLoggerBase loggerBase) {
        return (TestEvent) Proxy.newProxyInstance(AuditLoggerFactory.class.getClassLoader(), new Class<?>[] { TestEvent.class },
                new ProxyEventAuditLogger(loggerBase));
    }

    private interface TestEvent {

        @AuditEvent(category = "testcat", message = "testmsg", level = LogLevel.WARNING)
        void testWithParams(Object... params);

        @AuditEvent(category = "testcat2", message = "testmsg2", level = LogLevel.INFO)
        void testWithoutParams();

        void notEvent();
    }
}
