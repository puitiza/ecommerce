package com.ecommerce.commentservice.kafka;

import io.cloudevents.CloudEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

/**
 * In this class we'll add all the manual configuration required for Observability to
 * work.
 */
@Configuration
public class KafkaConsumerMetricsConfiguration {

    private final ConcurrentKafkaListenerContainerFactory<String, CloudEvent> concurrentKafkaListenerContainerFactory;

    public KafkaConsumerMetricsConfiguration(ConcurrentKafkaListenerContainerFactory<String, CloudEvent> factory) {
        this.concurrentKafkaListenerContainerFactory = factory;
    }

    @PostConstruct
    void setup() {
        this.concurrentKafkaListenerContainerFactory.getContainerProperties().setObservationEnabled(true);
    }

}