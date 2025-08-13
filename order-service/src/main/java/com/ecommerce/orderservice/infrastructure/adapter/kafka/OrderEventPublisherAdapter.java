package com.ecommerce.orderservice.infrastructure.adapter.kafka;

import com.ecommerce.orderservice.domain.event.OrderEvent;
import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.port.OrderEventPublisherPort;
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
    public void publishValidationSucceededEvent(Order order) {
        publishEvent(order, OrderEventType.VALIDATION_SUCCEEDED);
    }

    @Override
    public void publishValidationFailedEvent(Order order) {
        publishEvent(order, OrderEventType.VALIDATION_FAILED);
    }

    @Override
    public void publishRetryValidationEvent(Order order) {
        publishEvent(order, OrderEventType.RETRY_VALIDATION);
    }

    @Override
    public void publishPaymentStartEvent(Order order) {
        publishEvent(order, OrderEventType.PAYMENT_START);
    }

    @Override
    public void publishPaymentSucceededEvent(Order order) {
        publishEvent(order, OrderEventType.PAYMENT_SUCCEEDED);
    }

    @Override
    public void publishPaymentFailedEvent(Order order) {
        publishEvent(order, OrderEventType.PAYMENT_FAILED);
    }

    @Override
    public void publishRetryPaymentEvent(Order order) {
        publishEvent(order, OrderEventType.RETRY_PAYMENT);
    }

    @Override
    public void publishShipmentStartEvent(Order order) {
        publishEvent(order, OrderEventType.SHIPMENT_START);
    }

    @Override
    public void publishShipmentSucceededEvent(Order order) {
        publishEvent(order, OrderEventType.SHIPMENT_SUCCEEDED);
    }

    @Override
    public void publishShipmentFailedEvent(Order order) {
        publishEvent(order, OrderEventType.SHIPMENT_FAILED);
    }

    @Override
    public void publishRetryShipmentEvent(Order order) {
        publishEvent(order, OrderEventType.RETRY_SHIPMENT);
    }

    @Override
    public void publishDeliveredEvent(Order order) {
        publishEvent(order, OrderEventType.DELIVERED);
    }

    @Override
    public void publishCancelEvent(Order order) {
        publishEvent(order, OrderEventType.CANCEL);
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