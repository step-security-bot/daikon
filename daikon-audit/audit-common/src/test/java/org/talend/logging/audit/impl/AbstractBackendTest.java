package org.talend.logging.audit.impl;

import static org.easymock.EasyMock.partialMockBuilder;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class AbstractBackendTest {

    @Test
    public void testThatDefaultBahaviourIsToHaveMessages() {
        AbstractBackend logger = partialMockBuilder(AbstractBackend.class).createMock();
        assertTrue(logger.enableMessageFormat());
    }

}
