package org.talend.daikon.cloudevent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.provider.ExtensionProvider;

public class TalendCloudEventExtensionTest {

    @Test
    public void writeExtension() {
        TalendCloudEventExtension talendCloudEventExtension = new TalendCloudEventExtension();

        UUID randomUUID = UUID.randomUUID();

        talendCloudEventExtension.setCorrelationid("correlationId");
        talendCloudEventExtension.setTenantid(randomUUID.toString());

        CloudEvent event = CloudEventBuilder.v03().withId("aaa").withSource(URI.create("http://localhost")).withType("example")
                .withExtension(talendCloudEventExtension).build();

        assertEquals(randomUUID.toString(), event.getExtension(TalendCloudEventExtension.TENANTID));
        assertEquals("correlationId", event.getExtension(TalendCloudEventExtension.CORRELATIONID));

    }

    @Test
    public void parseExtension() {
        UUID randomUUID = UUID.randomUUID();

        CloudEvent event = CloudEventBuilder.v03().withId("aaa").withSource(URI.create("http://localhost")).withType("example")
                .withExtension(TalendCloudEventExtension.TENANTID, randomUUID.toString())
                .withExtension(TalendCloudEventExtension.CORRELATIONID, "correlationId").build();

        // register extension
        ExtensionProvider.getInstance().registerExtension(TalendCloudEventExtension.class, TalendCloudEventExtension::new);

        TalendCloudEventExtension talendCloudEventExtension = ExtensionProvider.getInstance()
                .parseExtension(TalendCloudEventExtension.class, event);

        assertNotNull(talendCloudEventExtension);
        assertEquals("correlationId", talendCloudEventExtension.getCorrelationid());
        assertEquals(randomUUID.toString(), talendCloudEventExtension.getTenantid());

    }
}
