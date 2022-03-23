package org.talend.logging.audit.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.talend.logging.audit.LogLevel;
import org.talend.logging.audit.impl.AuditConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class KafkaBackendTest {

    private KafkaBackend kafkaBackend;

    @Test
    public void testKafkaProducerInitialization() {
        kafkaBackend = new KafkaBackend(AuditConfiguration.loadFromClasspath("/audit.full.properties"));

        assertEquals("testTopic", kafkaBackend.getKafkaTopic());
        assertEquals("tenantId", kafkaBackend.getPartitionKeyName());
        assertEquals("localhost:9092", kafkaBackend.getBootstrapServers());
        assertEquals((Long) 30000L, kafkaBackend.getBlockTimeoutMs());
    }

    @Test
    public void testKafkaProducerMinimalInitialization() {
        kafkaBackend = new KafkaBackend(AuditConfiguration.loadFromClasspath("/audit.minimal.properties"));

        assertEquals("testTopic", kafkaBackend.getKafkaTopic());
        assertNull(kafkaBackend.getPartitionKeyName());
        assertEquals("localhost:9092", kafkaBackend.getBootstrapServers());
        assertEquals((Long) 60000L, kafkaBackend.getBlockTimeoutMs());
    }

    @Test
    public void testLogEmptyMap() {
        KafkaProducer<String, String> kafkaProducerMock = mock(KafkaProducer.class);
        Future futureMock = mock(Future.class);
        kafkaBackend = new KafkaBackend(kafkaProducerMock, "testTopic", "partitionKey", "localhost", 30000L);

        ArgumentCaptor<ProducerRecord<String, String>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        when(kafkaProducerMock.send(captor.capture())).thenReturn(futureMock);
        kafkaBackend.log("application security", LogLevel.INFO, "message", new Throwable());

        ProducerRecord<String, String> record = captor.getValue();
        assertNotNull(record);
        assertNull(record.key());
        assertEquals("null", record.value());
    }

    @Test
    public void testLogEventMap() {
        KafkaProducer<String, String> kafkaProducerMock = mock(KafkaProducer.class);
        Future futureMock = mock(Future.class);
        kafkaBackend = new KafkaBackend(kafkaProducerMock, "testTopic", "partitionKey", "localhost", 30000L);

        Map<String, String> eventMap = new HashMap<>();
        eventMap.put("partitionKey", "ID1234");
        eventMap.put("type", "audit");
        eventMap.put("operation", "read");
        kafkaBackend.setContextMap(eventMap);

        ArgumentCaptor<ProducerRecord<String, String>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        when(kafkaProducerMock.send(captor.capture())).thenReturn(futureMock);
        kafkaBackend.log("application security", LogLevel.INFO, "message", new Throwable());

        ProducerRecord<String, String> record = captor.getValue();
        assertNotNull(record);
        assertEquals("ID1234", record.key());
        assertEquals("{\"partitionKey\":\"ID1234\",\"type\":\"audit\",\"operation\":\"read\"}", record.value());
    }

    @Test
    public void testGetAndSetContextMap() {
        kafkaBackend = new KafkaBackend(AuditConfiguration.loadFromClasspath("/audit.minimal.properties"));

        Map<String, String> eventMap = new HashMap<>();
        eventMap.put("partitionKey", "ID1234");

        kafkaBackend.setContextMap(eventMap);

        Map<String, String> copyOfContextMap = kafkaBackend.getCopyOfContextMap();

        assertEquals(1, copyOfContextMap.size());
        assertEquals("ID1234", eventMap.get("partitionKey"));
    }
}