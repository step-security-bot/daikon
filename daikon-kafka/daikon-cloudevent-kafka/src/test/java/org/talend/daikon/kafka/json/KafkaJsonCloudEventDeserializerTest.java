package org.talend.daikon.kafka.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.CloudEventUtils;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.jackson.PojoCloudEventDataMapper;
import io.cloudevents.kafka.CloudEventSerializer;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.schemaregistry.json.JsonSchema;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KafkaJsonCloudEventDeserializerTest {

    @Test
    public void should_deserialize_as_cloudevent() {

        var registry = new MockSchemaRegistryClient();

        // setup
        var topic = "my-topic";

        var data = new MyData("payload");

        var serializer = new KafkaJsonCloudEventSerializer(registry);
        var deserializer = new KafkaJsonCloudEventDeserializer<MyData>(registry);

        Map<String, Object> configs = new HashMap<>();

        configs.put(CloudEventSerializer.ENCODING_CONFIG, "BINARY");
        configs.put("auto.register.schemas", "true");
        configs.put("schema.registry.url", "http://localhost:8081");

        serializer.configure(configs, Boolean.FALSE);
        deserializer.configure(configs, Boolean.FALSE);

        ObjectMapper objectMapper = new ObjectMapper();
        var event = CloudEventBuilder.v1().withId(UUID.randomUUID().toString()).withSource(URI.create("/example"))
                .withType(data.getClass().getName()).withTime(OffsetDateTime.now()).withDataContentType("application/json")
                .withData(PojoCloudEventData.wrap(data, objectMapper::writeValueAsBytes)).build();

        Headers headers = new RecordHeaders();
        var bytes = serializer.serialize(topic, headers, event);

        var actual = deserializer.deserialize(topic, headers, bytes);

        // assert
        assertTrue(actual.getData() instanceof PojoCloudEventData<?>);

        var ceData = CloudEventUtils.mapData(actual, PojoCloudEventDataMapper.from(new ObjectMapper(), MyData.class));

        // checking the data
        assertEquals("payload", ceData.getValue().payload());

        // checking the headers
        assertEquals(event.getSource(), URI.create(new String(headers.lastHeader("ce_source").value())));
        assertEquals(event.getSpecVersion(), SpecVersion.parse(new String(headers.lastHeader("ce_specversion").value())));
        assertEquals(event.getId(), new String(headers.lastHeader("ce_id").value()));
        assertEquals(event.getType(), new String(headers.lastHeader("ce_type").value()));

        serializer.close();
        deserializer.close();
    }

}
