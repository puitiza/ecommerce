package com.ecommerce.orderservice.infrastructure.configuration;

import io.cloudevents.CloudEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Configures Kafka for the order service, including listener factory, error handling, and topics.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderKafkaConfig {

    private final KafkaTemplate<String, CloudEvent> kafkaTemplate;
    private final ConsumerFactory<String, CloudEvent> consumerFactory;

    /**
     * Configures the Kafka listener container factory for processing CloudEvent messages.
     * Sets manual immediate acknowledgment and a single-threaded concurrency model.
     *
     * @return the configured listener container factory
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CloudEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CloudEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1); // single-threaded consumption
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(orderKafkaErrorHandler());
        return factory;
    }

    /**
     * Configures error handling with a DeadLetterPublishingRecoverer for failed messages.
     * Uses a fixed backoff policy with 3 retries and a -1 second interval.
     * Logs retry attempts for monitoring.
     *
     * @return the configured error handler
     */
    @Bean
    public CommonErrorHandler orderKafkaErrorHandler() {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate, (record, ex) -> new TopicPartition("order-dead-letter-topic", 0)
        );
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
        errorHandler.setRetryListeners((record, ex, attempt) ->
                log.warn("Retry attempt {} for record on topic {}: {}", attempt, record.topic(), ex.getMessage())
        );
        return errorHandler;
    }

    /**
     * Defines Kafka topics for the order service, including the dead-letter topic.
     *
     * @return an array of configured topics
     */
    @Bean
    public NewTopic[] orderTopics() {
        return new NewTopic[]{
                TopicBuilder.name("order-dead-letter-topic").partitions(1).replicas(1).build()
        };
    }
}
