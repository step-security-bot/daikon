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
package org.talend.daikon.messages.header;

import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.talend.daikon.messages.MessageEnvelope;
import org.talend.daikon.messages.MessageHeader;
import org.talend.daikon.messages.MessagePayload;
import org.talend.daikon.messages.header.consumer.CorrelationIdSetter;
import org.talend.daikon.messages.header.consumer.ExecutionContextUpdater;
import org.talend.daikon.messages.header.consumer.ExecutionContextUpdaterImpl;
import org.talend.daikon.messages.header.consumer.SecurityTokenSetter;
import org.talend.daikon.messages.header.consumer.ServiceAccountIdSetter;
import org.talend.daikon.messages.header.consumer.TenantIdSetter;
import org.talend.daikon.messages.header.consumer.UserIdSetter;

public class TestExecutionContextUpdaterImpl {

    private SecurityTokenSetter securityTokenSetter;

    private UserIdSetter userIdSetter;

    private TenantIdSetter tenantIdSetter;

    private CorrelationIdSetter correlationIdSetter;

    private ServiceAccountIdSetter serviceAccountIdSetter;

    @BeforeEach
    public void initMock() {
        securityTokenSetter = Mockito.mock(SecurityTokenSetter.class);
        userIdSetter = Mockito.mock(UserIdSetter.class);
        tenantIdSetter = Mockito.mock(TenantIdSetter.class);
        correlationIdSetter = Mockito.mock(CorrelationIdSetter.class);
        serviceAccountIdSetter = Mockito.mock(ServiceAccountIdSetter.class);
    }

    @Test
    public void testExecutionContextUpdater() {
        ExecutionContextUpdater executionContextUpdater = createExecutionContextUpdater();
        MessageEnvelope me = MessageEnvelope.newBuilder().setHeader(new MessageHeader())
                .setPayload(MessagePayload.newBuilder().setContent("content").setFormat("format").build()).build();
        executionContextUpdater.updateExecutionContext(me);

        Mockito.verify(correlationIdSetter, Mockito.times(1)).setCurrentCorrelationId(any());
        Mockito.verify(securityTokenSetter, Mockito.times(1)).setCurrentSecurityToken(any());
        Mockito.verify(userIdSetter, Mockito.times(1)).setCurrentUserId(any());
        Mockito.verify(tenantIdSetter, Mockito.times(1)).setCurrentTenantId(any());
        Mockito.verify(serviceAccountIdSetter, Mockito.times(1)).setCurrentServiceAccountId(any());
    }

    @Test
    public void testEmptyExecutionContextUpdater() {
        ExecutionContextUpdater executionContextUpdater = createEmptyExecutionContextUpdater();
        MessageEnvelope me = MessageEnvelope.newBuilder().setHeader(new MessageHeader())
                .setPayload(MessagePayload.newBuilder().setContent("content").setFormat("format").build()).build();
        executionContextUpdater.updateExecutionContext(me);

        Mockito.verify(correlationIdSetter, Mockito.times(0)).setCurrentCorrelationId(any());
        Mockito.verify(securityTokenSetter, Mockito.times(0)).setCurrentSecurityToken(any());
        Mockito.verify(userIdSetter, Mockito.times(0)).setCurrentUserId(any());
        Mockito.verify(tenantIdSetter, Mockito.times(0)).setCurrentTenantId(any());
        Mockito.verify(serviceAccountIdSetter, Mockito.times(0)).setCurrentServiceAccountId(any());
    }

    private ExecutionContextUpdater createExecutionContextUpdater() {
        return new ExecutionContextUpdaterImpl(correlationIdSetter, tenantIdSetter, userIdSetter, securityTokenSetter,
                serviceAccountIdSetter);
    }

    private ExecutionContextUpdater createEmptyExecutionContextUpdater() {
        return new ExecutionContextUpdaterImpl(null, null, null, null, null);
    }

}
