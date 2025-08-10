package com.ecommerce.orderservice.domain.event;

import lombok.Getter;

@Getter
public enum OrderEventType {

    ORDER_CREATED("order_created", "OrderCreatedEvent", "order.created"),
    ORDER_VALIDATED_SUCCESS("order_validated", "OrderValidatedSuccessEvent", "order.validated.success"),
    ORDER_VALIDATED_FAILED("validation_failed", "OrderValidatedFailedEvent", "order.validated.failed"),
    ORDER_UPDATED("order_updated", "OrderUpdatedEvent", "order.updated"),
    ORDER_CANCELLED("order_cancelled", "OrderCancelledEvent", "order.cancelled");

    private final String topic;
    private final String eventType;
    private final String subject;

    OrderEventType(String topic, String eventType, String subject) {
        this.topic = topic;
        this.eventType = eventType;
        this.subject = subject;
    }
}
