// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.daikon.messages.keys;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.talend.daikon.messages.MessageKey;
import org.talend.daikon.messages.header.producer.TenantIdProvider;

public class TestMessageKeyFactoryImpl {

    private static final String TENANT_ID = "testTenantId";

    private static final String RANDOM = "randomValue";

    private final TenantIdProvider tenantIdProvider = Mockito.mock(TenantIdProvider.class);

    private final MessageKeyFactoryImpl.RandomGenerator randomGenerator = Mockito
            .mock(MessageKeyFactoryImpl.RandomGenerator.class);

    @BeforeEach
    public void setup() throws Exception {
        Mockito.when(tenantIdProvider.getTenantId()).thenReturn(TENANT_ID);

        Mockito.when(randomGenerator.generateRandom()).thenReturn(RANDOM);
    }

    @Test
    public void testCreateKeyWithDefaultRandomProvider() {
        MessageKeyFactory factory = new MessageKeyFactoryImpl(tenantIdProvider);

        MessageKey messageKey1 = factory.createMessageKey();

        MessageKey messageKey2 = factory.createMessageKey();

        assertFalse(messageKey1.equals(messageKey2));

        assertFalse(messageKey1.hashCode() == messageKey2.hashCode());

        assertEquals(messageKey1.getTenantId(), messageKey2.getTenantId());
    }

    @Test
    public void testCreateKeyWithCustomRandomProvider() {
        MessageKeyFactory factory = new MessageKeyFactoryImpl(tenantIdProvider, randomGenerator);

        MessageKey messageKey1 = factory.createMessageKey();

        MessageKey messageKey2 = factory.createMessageKey();

        assertTrue(messageKey1.equals(messageKey2));

        assertTrue(messageKey1.hashCode() == messageKey2.hashCode());

        assertEquals(messageKey1.getTenantId(), messageKey2.getTenantId());

        assertEquals(messageKey1.getRandom(), messageKey2.getRandom());
    }

    @Test
    public void testBuildKeyWithCustomPartitioningKeys() {
        MessageKeyFactory factory = new MessageKeyFactoryImpl(tenantIdProvider);

        MessageKey messageKey1 = factory.buildMessageKey().withKey("KEY1", "VALUE1").withKey("KEY2", "VALUE2").build();

        MessageKey messageKey2 = factory.buildMessageKey().withKey("KEY1", "VALUE1").withKey("KEY2", "VALUE2").build();

        assertTrue(messageKey1.equals(messageKey2));

        assertTrue(messageKey1.hashCode() == messageKey2.hashCode());

        assertEquals(messageKey1.getTenantId(), messageKey2.getTenantId());

        assertEquals(messageKey1.getKeys(), messageKey2.getKeys());

    }

}
