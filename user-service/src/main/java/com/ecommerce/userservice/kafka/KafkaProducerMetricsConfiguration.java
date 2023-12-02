package com.ecommerce.userservice.kafka;

import io.cloudevents.CloudEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Configures observability for the Kafka producer by enabling necessary settings.
 */
@Configuration
public class KafkaProducerMetricsConfiguration {

    private final KafkaTemplate<String, CloudEvent> kafkaTemplate;

    public KafkaProducerMetricsConfiguration(KafkaTemplate<String, CloudEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    void setup() {
        this.kafkaTemplate.setObservationEnabled(true);
    }

}
