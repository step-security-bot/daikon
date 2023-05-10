package org.talend.daikon.kafka.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.cloudevents.kafka.CloudEventDeserializer;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaDeserializer;
import org.apache.kafka.common.header.Headers;

import java.util.Map;

public class KafkaJsonCloudEventDeserializer<T> extends CloudEventDeserializer {

    private final KafkaJsonSchemaDeserializer<T> kafkaJsonSchemaDeserializer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public KafkaJsonCloudEventDeserializer(SchemaRegistryClient client) {
        super();
        this.kafkaJsonSchemaDeserializer = new KafkaJsonSchemaDeserializer<>(client);
    }

    public KafkaJsonCloudEventDeserializer() {
        super();
        this.kafkaJsonSchemaDeserializer = new KafkaJsonSchemaDeserializer<>();
    }

    @Override
    public void configure(Map config, boolean isKey) {
        super.configure(config, isKey);
        kafkaJsonSchemaDeserializer.configure(config, isKey);
    }

    @Override
    public CloudEvent deserialize(String topic, Headers headers, byte[] data) {
        // first we deserialize with confluent deserializer in order to get a T object according to the schema
        T value = kafkaJsonSchemaDeserializer.deserialize(topic, headers, data);

        // Then we deserialize with cloud event deserializer in order to get cloud event based on header
        var event = super.deserialize(topic, headers, data);

        // finally we wrap the T object into a PojoCloudEventData and set it as data of the cloud event
        return new CloudEventBuilder(event)
                .withData(PojoCloudEventData.wrap(value, kafkaJsonSchemaDeserializer.objectMapper()::writeValueAsBytes)).build();
    }

    @Override
    public void close() {
        super.close();
        kafkaJsonSchemaDeserializer.close();
    }
}
