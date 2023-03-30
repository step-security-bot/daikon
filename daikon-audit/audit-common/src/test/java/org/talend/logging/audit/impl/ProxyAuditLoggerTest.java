package org.talend.logging.audit.impl;

import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.lang.reflect.Proxy;

import org.junit.jupiter.api.Test;
import org.talend.logging.audit.AuditLogger;
import org.talend.logging.audit.AuditLoggerFactory;
import org.talend.logging.audit.Context;
import org.talend.logging.audit.ContextBuilder;
import org.talend.logging.audit.LogLevel;

public class ProxyAuditLoggerTest {

    @Test
    public void testAuditLogger() {
        String category = "testcat";
        String message = "testmsg";
        Context ctx = ContextBuilder.emptyContext();
        Throwable thr = new IllegalStateException();

        AuditLoggerBase base = mock(AuditLoggerBase.class);
        base.log(LogLevel.ERROR, category, ctx, thr, message);
        replay(base);

        AuditLogger auditLogger = getAuditLogger(base);

        auditLogger.error(category, ctx, thr, message);

        verify(base);
    }

    private static AuditLogger getAuditLogger(AuditLoggerBase loggerBase) {
        return (AuditLogger) Proxy.newProxyInstance(AuditLoggerFactory.class.getClassLoader(),
                new Class<?>[] { AuditLogger.class }, new ProxyAuditLogger(loggerBase));
    }
}
