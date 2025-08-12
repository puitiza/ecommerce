package com.ecommerce.orderservice.infrastructure.adapter.kafka;

import com.ecommerce.orderservice.domain.port.OrderEventPublisherPort;
import com.ecommerce.orderservice.domain.event.OrderEvent;
import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.model.Order;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
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

    @Override
    public void publishOrderCreatedEvent(Order order) {
        publishEvent(order, OrderEventType.ORDER_CREATED);
    }

    @Override
    public void publishOrderValidatedEvent(Order order) {
        publishEvent(order, OrderEventType.ORDER_VALIDATED_SUCCESS);
    }

    @Override
    public void publishValidationFailedEvent(Order order) {
        publishEvent(order, OrderEventType.ORDER_VALIDATED_FAILED);
    }

    @Override
    public void publishOrderUpdatedEvent(Order order) {
        publishEvent(order, OrderEventType.ORDER_UPDATED);
    }

    @Override
    public void publishOrderCancelledEvent(Order order) {
        publishEvent(order, OrderEventType.ORDER_CANCELLED);
    }

    private void publishEvent(Order order, OrderEventType eventType) {
        CloudEvent cloudEvent = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withType(eventType.getEventType())
                .withSource(URI.create("/order-service"))
                .withDataContentType("application/json")
                .withData(new OrderEvent(order))
                .withSubject("com.ecommerce.event.report." + eventType.getSubject())
                .build();
        kafkaTemplate.send(eventType.getTopic(), UUID.randomUUID().toString(), cloudEvent);
        log.info("Sent '{}' event to Kafka for order ID: {}", eventType.getEventType(), order.id());
    }
}
