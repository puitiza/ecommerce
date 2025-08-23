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
 * Kafka configuration for the product service, leveraging Spring Boot autoconfiguration.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ProductKafkaConfig {

    private final KafkaTemplate<String, CloudEvent> kafkaTemplate;
    // 1. Inyecta el ConsumerFactory autoconfigurado por Spring
    private final ConsumerFactory<String, CloudEvent> consumerFactory;


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CloudEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CloudEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        // 2. Asigna el consumerFactory al factory
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);// Single thread to avoid duplicate processing
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(productKafkaErrorHandler());
        return factory;
    }

    @Bean
    public CommonErrorHandler productKafkaErrorHandler() {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate, (record, ex) -> new TopicPartition("product-dead-letter-topic", -1)
        );
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
        errorHandler.setRetryListeners((record, ex, attempt) ->
                log.info("Retry attempt {} for record on topic {}: {}", attempt, record.topic(), ex.getMessage())
        );
        return errorHandler;
    }

    @Bean
    public NewTopic[] productTopics() {
        return new NewTopic[]{
                TopicBuilder.name(ProductEventType.PRODUCT_VALIDATION_SUCCEEDED.getTopic()).partitions(1).replicas(1).build(),
                TopicBuilder.name(ProductEventType.PRODUCT_VALIDATION_FAILED.getTopic()).partitions(1).replicas(1).build(),
                TopicBuilder.name(ProductEventType.PRODUCT_INVENTORY_UPDATED.getTopic()).partitions(1).replicas(1).build(),
                TopicBuilder.name("product-dead-letter-topic").partitions(1).replicas(1).build()
        };
    }
}