package com.ecommerce.productservice.infrastructure.adapter.kafka;

import com.ecommerce.productservice.domain.event.ProductEventType;
import com.ecommerce.productservice.domain.model.Product;
import com.ecommerce.productservice.domain.port.out.ProductEventPublisherPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.UUID;

/**
 * Kafka adapter for publishing product-related events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventPublisherAdapter implements ProductEventPublisherPort {

    private final KafkaTemplate<String, CloudEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(Product product, ProductEventType eventType) {
        try {
            PojoCloudEventData<Product> cloudEventData = PojoCloudEventData.wrap(product, objectMapper::writeValueAsBytes);
            CloudEvent cloudEvent = CloudEventBuilder.v1()
                    .withId(UUID.randomUUID().toString())
                    .withType(eventType.getEventType())
                    .withSource(URI.create("/product-service"))
                    .withDataContentType("application/json")
                    .withData(cloudEventData)
                    .withSubject("com.ecommerce.event.report." + eventType.getSubject())
                    .build();
            kafkaTemplate.send(eventType.getTopic(), product.id().toString(), cloudEvent);
            log.info("Published {} event for product ID: {}", eventType.getEventType(), product.id());
        } catch (Exception e) {
            log.error("Failed to publish {} event for product ID: {}", eventType.getEventType(), product.id(), e);
            throw new RuntimeException("Failed to publish product event", e);
        }
    }

}