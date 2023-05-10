package org.talend.daikon.kafka.example.consumer;

import org.talend.daikon.kafka.example.consumer.pojo.Data;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.CloudEventUtils;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.jackson.PojoCloudEventDataMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;

@SpringBootApplication
public class ExampleConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleConsumerApplication.class, args);
    }

    @KafkaListener(topics = "my-topic", containerFactory = "kafkaListenerContainerFactory")
    public void listenGroupFoo(CloudEvent cloudEvent) {
        System.out.println("Received Message: " + cloudEvent);
        PojoCloudEventData<Data> cloudEventData = CloudEventUtils.mapData(cloudEvent,
                PojoCloudEventDataMapper.from(new ObjectMapper(), Data.class));
        System.out.println("cloudEventData.getValue() = " + cloudEventData.getValue());
    }
}
