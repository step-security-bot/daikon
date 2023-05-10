package org.talend.daikon.kafka.example.producer;

import org.talend.daikon.kafka.example.producer.pojo.After;
import org.talend.daikon.kafka.example.producer.pojo.Data;
import org.talend.daikon.kafka.example.producer.pojo.Payload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.talend.daikon.cloudevent.TalendCloudEventExtension;
import org.talend.daikon.kafka.json.KafkaJsonCloudEventSerializer;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class ExampleProducerApplication {

    public static void main(String[] args) throws JsonProcessingException {

        SpringApplication.run(ExampleProducerApplication.class, args);

        ObjectMapper objectMapper = new ObjectMapper();

        try (KafkaProducer<String, CloudEvent> producer = new KafkaProducer<>(producerFactory())) {

            Data data = new Data(new Payload(new After("2021-07-01", "123"), null));

            TalendCloudEventExtension tce = new TalendCloudEventExtension();
            tce.setTenantid("tenantId");
            tce.setCorrelationid("123");

            // Build an event
            CloudEvent event = CloudEventBuilder.v1().withId("hello").withType("example.kafka")
                    .withSource(URI.create("http://localhost")).withDataContentType("application/json")
                    .withData(PojoCloudEventData.wrap(data, objectMapper::writeValueAsBytes)).withExtension(tce).build();

            // Produce the event
            producer.send(new ProducerRecord("my-topic", event));
        }
    }

    public static Map<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonCloudEventSerializer.class);
        configProps.put("schema.registry.url", "http://localhost:8081");
        configProps.put("auto.register.schemas", false);
        configProps.put("use.latest.version", true);
        configProps.put("json.fail.invalid.schema", true);
        configProps.put("latest.compatibility.strict", false);
        return configProps;
    }

}
