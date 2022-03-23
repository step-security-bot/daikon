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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.junit.jupiter.api.Test;
import org.talend.daikon.messages.MessageHeader;
import org.talend.daikon.messages.MessageIssuer;
import org.talend.daikon.messages.MessageTypes;
import org.talend.daikon.messages.header.consumer.MessageHeaderExtractor;

import java.io.IOException;

public class TestMessageHeaderExtractor {

    @Test
    public void testHiddenSecuredField() {
        MessageHeader messageHeader = MessageHeader.newBuilder().setId("My id") //
                .setCorrelationId("Correlation id") //
                .setTimestamp(123L) //
                .setIssuer(MessageIssuer.newBuilder().setApplication("Application1").setService("Service1").setVersion("ABC")
                        .build()) //
                .setType(MessageTypes.COMMAND).setName("name").setTenantId("tenantId").setUserId("userId")
                .setServiceAccountId("serviceAccountId") //
                .setSecurityToken("securityToken").build();

        assertNotNull(messageHeader.toString());
        assertTrue(messageHeader.toString().contains("\"securityToken\": <hidden>"));
    }

    @Test
    public void testExtractMessageHeaderWithSpecificRecord() throws Exception {
        Schema headerSchema = loadMessageHeaderSchema();
        Schema messageSchema = SchemaBuilder.record("message").fields().name("header").type(headerSchema).noDefault()
                .name("customField").type().stringType().noDefault().endRecord();

        MessageHeader messageHeader = MessageHeader.newBuilder().setId("My id").setCorrelationId("Correlation id")
                .setTimestamp(123L)
                .setIssuer(MessageIssuer.newBuilder().setApplication("Application1").setService("Service1").setVersion("ABC")
                        .build())
                .setType(MessageTypes.COMMAND).setName("name").setTenantId("tenantId").setUserId("userId")
                .setServiceAccountId("serviceAccountId").setSecurityToken("securityToken").build();

        IndexedRecord message = new GenericRecordBuilder(messageSchema).set("header", messageHeader).set("customField", "ABC")
                .build();

        MessageHeaderExtractor extractor = new MessageHeaderExtractor();
        MessageHeader output = extractor.extractHeader(message);
        assertEquals(messageHeader, output);
    }

    @Test
    public void testGetMessageHeaderWithGenericRecord() throws Exception {
        Schema headerSchema = loadMessageHeaderSchema();
        Schema messageSchema = SchemaBuilder.record("message").fields().name("header").type(headerSchema).noDefault()
                .name("customField").type().stringType().noDefault().endRecord();

        IndexedRecord issuer = new GenericRecordBuilder(headerSchema.getField("issuer").schema())
                .set("application", "Application1").set("service", "Service1").set("version", "ABC").build();

        IndexedRecord messageHeader = new GenericRecordBuilder(headerSchema).set("id", "My id")
                .set("correlationId", "Correlation id").set("timestamp", 123L).set("issuer", issuer).set("type", "COMMAND")
                .set("name", "name").set("tenantId", "tenantId").set("userId", "userId").set("securityToken", "securityToken")
                .set("serviceAccountId", "serviceAccountId").build();

        IndexedRecord message = new GenericRecordBuilder(messageSchema).set("header", messageHeader).set("customField", "ABC")
                .build();

        MessageHeaderExtractor extractor = new MessageHeaderExtractor();
        MessageHeader output = extractor.extractHeader(message);
        assertNotNull(output);
        assertEquals(0, GenericData.get().compare(messageHeader, output, headerSchema));
    }

    @Test
    public void testStringAsFirstField() {
        IllegalArgumentException expectedException = assertThrows(IllegalArgumentException.class, () -> {
            Schema messageSchema = SchemaBuilder.record("message").fields().name("fakeHeader").type().stringType().noDefault()
                    .endRecord();

            IndexedRecord message = new GenericRecordBuilder(messageSchema).set("fakeHeader", "hello").build();

            MessageHeaderExtractor extractor = new MessageHeaderExtractor();
            extractor.extractHeader(message);

        });
        assertEquals("Provided message's first field is not a record but STRING", expectedException.getMessage());
    }

    @Test
    public void testUnknownRecordAsFirstField() {
        IllegalArgumentException expectedException = assertThrows(IllegalArgumentException.class, () -> {
            Schema firstFieldSchema = SchemaBuilder.record("firstField").fields().name("field1").type().stringType().noDefault()
                    .endRecord();

            Schema messageSchema = SchemaBuilder.record("message").fields().name("fakeHeader").type(firstFieldSchema).noDefault()
                    .endRecord();

            IndexedRecord firstField = new GenericRecordBuilder(firstFieldSchema).set("field1", "value1").build();

            IndexedRecord message = new GenericRecordBuilder(messageSchema).set("fakeHeader", firstField).build();

            MessageHeaderExtractor extractor = new MessageHeaderExtractor();
            extractor.extractHeader(message);
        });
        assertEquals("Provided message's first field is not a header but firstField", expectedException.getMessage());
    }

    private Schema loadMessageHeaderSchema() throws IOException {
        return new Schema.Parser().parse(this.getClass().getResourceAsStream("/MessageHeader.avsc"));
    }

}
