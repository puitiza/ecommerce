package com.ecommerce.orderservice.domain.event;

import com.ecommerce.shared.domain.event.SharedOrderEvent;
import lombok.Getter;

/**
 * Enum representing types of events in the order lifecycle.
 * <p>
 * Each event type corresponds to a Kafka topic, CloudEvent type, and subject for structured event publishing.
 */
@Getter
public enum OrderEventType {

    // === Shared events ===
    ORDER_CREATED(SharedOrderEvent.ORDER_CREATED),
    VALIDATION_SUCCEEDED(SharedOrderEvent.VALIDATION_SUCCEEDED),
    VALIDATION_FAILED(SharedOrderEvent.VALIDATION_FAILED),
    RETRY_VALIDATION(SharedOrderEvent.RETRY_VALIDATION),
    PAYMENT_START(SharedOrderEvent.PAYMENT_START),
    PAYMENT_SUCCEEDED(SharedOrderEvent.PAYMENT_SUCCEEDED),
    PAYMENT_FAILED(SharedOrderEvent.PAYMENT_FAILED),
    RETRY_PAYMENT(SharedOrderEvent.RETRY_PAYMENT),
    SHIPMENT_START(SharedOrderEvent.SHIPMENT_START),
    SHIPMENT_SUCCEEDED(SharedOrderEvent.SHIPMENT_SUCCEEDED),
    SHIPMENT_FAILED(SharedOrderEvent.SHIPMENT_FAILED),
    RETRY_SHIPMENT(SharedOrderEvent.RETRY_SHIPMENT),
    DELIVERED(SharedOrderEvent.DELIVERED),
    CANCEL(SharedOrderEvent.CANCEL),
    AUTO_CANCEL(SharedOrderEvent.AUTO_CANCEL),

    // === Local events (only order-service) ===
    ORDER_UPDATED("order_updated", "OrderUpdatedEvent", "order.updated"),
    AUTO_VALIDATE("auto_validate", "AutoValidateEvent", "order.auto.validate"),
    ORDER_CONFIRMED("confirm_order", "ConfirmOrderEvent", "order.confirm_order");

    private final String topic;
    private final String eventType;
    private final String subject;

    // Builder for the shared
    OrderEventType(SharedOrderEvent shared) {
        this.topic = shared.getTopic();
        this.eventType = shared.getEventType();
        this.subject = shared.getSubject();
    }

    OrderEventType(String topic, String eventType, String subject) {
        this.topic = topic;
        this.eventType = eventType;
        this.subject = subject;
    }
}