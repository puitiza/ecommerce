package com.ecommerce.productservice.infrastructure.adapter.kafka;

import com.ecommerce.productservice.domain.port.out.OrderEventPublisherPort;
import com.ecommerce.shared.domain.event.OrderEventPayload;
import com.ecommerce.shared.domain.event.SharedOrderEvent;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisherAdapter implements OrderEventPublisherPort {

    private final KafkaTemplate<String, CloudEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publishValidationSucceeded(OrderEventPayload order) {
        publishEvent(order, SharedOrderEvent.VALIDATION_SUCCEEDED);
    }

    @Override
    public void publishValidationFailed(OrderEventPayload order) {
        publishEvent(order, SharedOrderEvent.VALIDATION_FAILED);
    }

    private void publishEvent(OrderEventPayload order, SharedOrderEvent eventType) {
        if (order == null) {
            log.warn("Attempted to publish event {} with null order", eventType.getEventType());
            return;
        }
        try {
            PojoCloudEventData<OrderEventPayload> cloudEventData = PojoCloudEventData.wrap(order, objectMapper::writeValueAsBytes);
            CloudEvent cloudEvent = CloudEventBuilder.v1()
                    .withId(UUID.randomUUID().toString())
                    .withType(eventType.getEventType())
                    .withSource(URI.create("/product-service"))
                    .withDataContentType("application/json")
                    .withData(cloudEventData)
                    .withSubject("com.ecommerce.event.report." + eventType.getSubject())
                    .build();

            kafkaTemplate.send(eventType.getTopic(), order.id().toString(), cloudEvent);
            log.info("Sent '{}' to Kafka for order ID: {}", eventType.getEventType(), order.id());
        } catch (Exception e) {
            log.error("Failed to publish {} event for order ID: {}", eventType.getEventType(), order.id(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

}