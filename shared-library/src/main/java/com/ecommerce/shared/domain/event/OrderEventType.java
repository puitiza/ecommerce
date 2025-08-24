package com.ecommerce.shared.domain.event;

import lombok.Getter;

/**
 * Enum defining order-related event types for inter-service communication.
 * Only includes events shared between order-service and product-service.
 */
@Getter
public enum OrderEventType {
    ORDER_CREATED("order_created", "OrderCreatedEvent", "order.created"),
    ORDER_UPDATED("order_updated", "OrderUpdatedEvent", "order.updated"),
    RETRY_VALIDATION("retry_validation", "RetryValidationEvent", "retry.validation"),
    VALIDATION_SUCCEEDED("validation_succeeded", "ValidationSucceededEvent", "validation.succeeded"),
    VALIDATION_FAILED("validation_failed", "ValidationFailedEvent", "validation.failed");

    private final String topic;
    private final String eventType;
    private final String subject;

    OrderEventType(String topic, String eventType, String subject) {
        this.topic = topic;
        this.eventType = eventType;
        this.subject = subject;
    }
}