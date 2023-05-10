package org.talend.daikon.kafka.json;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.kafka.CloudEventSerializer;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.json.KafkaJsonSchemaSerializer;
import org.apache.kafka.common.header.Headers;

import java.util.Map;

public class KafkaJsonCloudEventSerializer extends CloudEventSerializer {

    private final KafkaJsonSchemaSerializer kafkaJsonSchemaSerializer;

    public KafkaJsonCloudEventSerializer() {
        super();
        this.kafkaJsonSchemaSerializer = new KafkaJsonSchemaSerializer<>();
    }

    public KafkaJsonCloudEventSerializer(SchemaRegistryClient client) {
        super();
        this.kafkaJsonSchemaSerializer = new KafkaJsonSchemaSerializer<>(client);
    }

    @Override
    public void configure(Map config, boolean isKey) {
        super.configure(config, isKey);
        kafkaJsonSchemaSerializer.configure(config, isKey);
    }

    @Override
    public byte[] serialize(String topic, Headers headers, CloudEvent ceEvent) {
        // first we serialize with the cloud event serializer in order to set cloud event headers
        super.serialize(topic, headers, ceEvent);
        // then we serialize the data with confluent serializer
        if (ceEvent.getData() instanceof PojoCloudEventData ceEventData) {
            return kafkaJsonSchemaSerializer.serialize(topic, headers, ceEventData.getValue());
        }
        return kafkaJsonSchemaSerializer.serialize(topic, headers, ceEvent.getData());
    }

    @Override
    public void close() {
        super.close();
        kafkaJsonSchemaSerializer.close();
    }
}
