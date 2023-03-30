package org.talend.logging.audit.impl;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.MockType.STRICT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.talend.logging.audit.LogLevel.INFO;

import java.util.Collections;
import java.util.Map;

import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EasyMockExtension.class)
public class SimpleAuditLoggerBaseTest {

    private SimpleAuditLoggerBase simpleAuditLoggerBase;

    @Mock(STRICT)
    public AbstractBackend backend;

    @Test
    public void testInitAutoNoKafkaBackendNotFound() {
        RuntimeException expectedException = assertThrows(RuntimeException.class, () -> {
            AuditConfigurationMap config = new AuditConfigurationMapImpl();
            config.setValue(AuditConfiguration.BACKEND, Backends.AUTO, Backends.class);
            simpleAuditLoggerBase = new SimpleAuditLoggerBase(config);

        });

        assertEquals("Unable to load backend org.talend.logging.audit.kafka.KafkaBackend", expectedException.getMessage());

    }

    @Test
    public void testInitKafkaBackendNotFound() {
        RuntimeException expectedException = assertThrows(RuntimeException.class, () -> {
            AuditConfigurationMap config = new AuditConfigurationMapImpl();
            config.setValue(AuditConfiguration.BACKEND, Backends.KAFKA, Backends.class);
            simpleAuditLoggerBase = new SimpleAuditLoggerBase(config);

        });

        assertEquals("Unable to load backend org.talend.logging.audit.kafka.KafkaBackend", expectedException.getMessage());

    }

    @Test
    public void testInitNotSupportedBackend() {
        IllegalArgumentException expectedException = assertThrows(IllegalArgumentException.class, () -> {
            AuditConfigurationMap config = new AuditConfigurationMapImpl();
            config.setValue(AuditConfiguration.BACKEND, Backends.LOGBACK, Backends.class);
            simpleAuditLoggerBase = new SimpleAuditLoggerBase(config);

        });

        assertEquals("Unsupported backend LOGBACK", expectedException.getMessage());
    }

    @Test
    public void testContextIsRestored() {
        simpleAuditLoggerBase = new SimpleAuditLoggerBase(backend);
        Map<String, String> currentContext = Collections.singletonMap("k", "v");

        expect(backend.getCopyOfContextMap()).andReturn(currentContext);
        expect(backend.setNewContext(anyObject())).andReturn(null);
        backend.log(anyObject(), anyObject(), anyObject(), anyObject());
        backend.resetContext(currentContext);

        replay(backend);
        simpleAuditLoggerBase.log(INFO, "category", null, null, "msg");
        verify(backend);
    }
}
