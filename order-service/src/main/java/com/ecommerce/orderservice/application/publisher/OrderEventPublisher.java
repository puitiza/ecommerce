package com.ecommerce.orderservice.application.publisher;

import com.ecommerce.orderservice.application.dto.OrderResponse;
import com.ecommerce.orderservice.application.event.OrderEventData;
import com.ecommerce.orderservice.application.event.OrderEventType;
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
public class OrderEventPublisher {
    private final KafkaTemplate<String, CloudEvent> kafkaTemplate;

    public void publishOrderCreatedEvent(Order order) {
        publishEvent(order, OrderEventType.ORDER_CREATED);
    }

    public void publishOrderValidatedEvent(Order order) {
        publishEvent(order, OrderEventType.ORDER_VALIDATED_SUCCESS);
    }

    public void publishValidationFailedEvent(Order order) {
        publishEvent(order, OrderEventType.ORDER_VALIDATED_FAILED);
    }

    public void publishOrderUpdatedEvent(Order order) {
        publishEvent(order, OrderEventType.ORDER_UPDATED);
    }

    public void publishOrderCancelledEvent(Order order) {
        publishEvent(order, OrderEventType.ORDER_CANCELLED);
    }

    private void publishEvent(Order order, OrderEventType eventType) {
        OrderResponse response = new OrderResponse(
                order.id().toString(),
                order.userId(),
                order.items().stream()
                        .map(item -> new com.ecommerce.orderservice.application.dto.OrderItemResponse(
                                item.productId(),
                                "Unknown", // Name fetched externally if needed
                                item.quantity(),
                                item.unitPrice().doubleValue()
                        ))
                        .toList(),
                order.status(),
                order.createdAt(),
                order.updatedAt(),
                order.totalPrice(),
                order.shippingAddress()
        );
        CloudEvent cloudEvent = createCloudOrderEvent(response, eventType);
        String kafkaRecordKey = UUID.randomUUID().toString();
        kafkaTemplate.send(eventType.getTopic(), kafkaRecordKey, cloudEvent);
        log.info("Sent '{}' event to Kafka for order ID: {}", eventType.getEventType(), order.id());
    }

    private CloudEvent createCloudOrderEvent(OrderResponse order, OrderEventType eventType) {
        OrderEventData eventData = new OrderEventData(order);
        return CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withType(eventType.getEventType())
                .withSource(URI.create("/order-service"))
                .withDataContentType("application/json")
                .withData(eventData)
                .withSubject("com.ecommerce.event.report." + eventType.getSubject())
                .build();
    }
}
