package com.ecommerce.orderservice.infrastructure.adapter.kafka;

import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.exception.EventPublishingException;
import com.ecommerce.orderservice.domain.port.out.OrderEventPublisherPort;
import com.ecommerce.shared.domain.event.OrderEventPayload;
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
 * Publishes order events to Kafka topics as CloudEvents.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisherAdapter implements OrderEventPublisherPort {

    private final KafkaTemplate<String, CloudEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publishOrderCreatedEvent(OrderEventPayload order) {
        publishEvent(order, OrderEventType.ORDER_CREATED);
    }

    @Override
    public void publishOrderUpdatedEvent(OrderEventPayload order) {
        publishEvent(order, OrderEventType.ORDER_UPDATED);
    }

    @Override
    public void publishAutoValidateEvent(OrderEventPayload order) {
        publishEvent(order, OrderEventType.AUTO_VALIDATE);
    }

    @Override
    public void publishValidationSucceededEvent(OrderEventPayload order) {
        publishEvent(order, OrderEventType.VALIDATION_SUCCEEDED);
    }

    @Override
    public void publishValidationFailedEvent(OrderEventPayload order) {
        publishEvent(order, OrderEventType.VALIDATION_FAILED);
    }

    @Override
    public void publishRetryValidationEvent(OrderEventPayload order) {
        publishEvent(order, OrderEventType.RETRY_VALIDATION);
    }

    @Override
    public void publishPaymentStartEvent(OrderEventPayload order) {
        publishEvent(order, OrderEventType.PAYMENT_START);
    }

    @Override
    public void publishPaymentSucceededEvent(OrderEventPayload order) {
        publishEvent(order, OrderEventType.PAYMENT_SUCCEEDED);
    }

    @Override
    public void publishPaymentFailedEvent(OrderEventPayload order) {
        publishEvent(order, OrderEventType.PAYMENT_FAILED);
    }

    @Override
    public void publishRetryPaymentEvent(OrderEventPayload order) {
        publishEvent(order, OrderEventType.RETRY_PAYMENT);
    }

    @Override
    public void publishShipmentStartEvent(OrderEventPayload order) {
        publishEvent(order, OrderEventType.SHIPMENT_START);
    }

    @Override
    public void publishShipmentSucceededEvent(OrderEventPayload order) {
        publishEvent(order, OrderEventType.SHIPMENT_SUCCEEDED);
    }

    @Override
    public void publishShipmentFailedEvent(OrderEventPayload order) {
        publishEvent(order, OrderEventType.SHIPMENT_FAILED);
    }

    @Override
    public void publishRetryShipmentEvent(OrderEventPayload order) {
        publishEvent(order, OrderEventType.RETRY_SHIPMENT);
    }

    @Override
    public void publishDeliveredEvent(OrderEventPayload order) {
        publishEvent(order, OrderEventType.DELIVERED);
    }

    @Override
    public void publishCancelEvent(OrderEventPayload order) {
        publishEvent(order, OrderEventType.CANCEL);
    }

    /**
     * Publishes an event to Kafka as a CloudEvent, handling serialization and error cases.
     *
     * @param order     the order event payload
     * @param eventType the type of event to publish
     * @throws EventPublishingException if publishing fails
     */
    private void publishEvent(OrderEventPayload order, OrderEventType eventType) {
        if (order == null) {
            log.warn("Attempted to publish event {} with null order", eventType.getEventType());
            return;
        }
        try {
            PojoCloudEventData<OrderEventPayload> cloudEventData = PojoCloudEventData.wrap(order, objectMapper::writeValueAsBytes);
            CloudEvent cloudEvent = CloudEventBuilder.v1()
                    .withId(UUID.randomUUID().toString())
                    .withType(eventType.getEventType())
                    .withSource(URI.create("/order-service"))
                    .withDataContentType("application/json")
                    .withData(cloudEventData)
                    .withSubject("com.ecommerce.event.report." + eventType.getSubject())
                    .build();

            kafkaTemplate.send(eventType.getTopic(), order.id().toString(), cloudEvent);
            log.info("Sent '{}' to Kafka for order ID: {}", eventType.getEventType(), order.id());
        } catch (Exception e) {
            log.error("Failed to publish {} event for order ID: {}", eventType.getEventType(), order.id(), e);
            throw new EventPublishingException("Failed to publish %s event for order %s".formatted(eventType.getEventType(), order.id()), e);
        }
    }
}