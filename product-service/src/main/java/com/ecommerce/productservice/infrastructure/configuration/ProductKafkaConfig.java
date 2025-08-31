package com.ecommerce.productservice.infrastructure.configuration;

import com.ecommerce.productservice.domain.event.ProductEventType;
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
 * Kafka configuration for the product service with centralized DLT.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ProductKafkaConfig {

    private final KafkaTemplate<String, CloudEvent> kafkaTemplate;
    private final ConsumerFactory<String, CloudEvent> consumerFactory;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CloudEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CloudEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(productKafkaErrorHandler());
        return factory;
    }

    @Bean
    public CommonErrorHandler productKafkaErrorHandler() {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate, (record, ex) -> new TopicPartition("product-dead-letter-topic", 0)
        );
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
        errorHandler.setRetryListeners((record, ex, attempt) ->
                log.warn("Retry attempt {} for record on topic {}: {}", attempt, record.topic(), ex.getMessage())
        );
        return errorHandler;
    }

    @Bean
    public NewTopic[] productTopics() {
        return new NewTopic[]{
                TopicBuilder.name(ProductEventType.PRODUCT_INVENTORY_UPDATED.getTopic()).partitions(1).replicas(1).build(),
                // Unique centralized dlt for product-service
                TopicBuilder.name("product-dead-letter-topic").partitions(1).replicas(1).build()
        };
    }
}