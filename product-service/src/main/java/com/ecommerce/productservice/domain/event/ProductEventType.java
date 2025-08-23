package com.ecommerce.productservice.domain.event;

import lombok.Getter;

/**
 * Enum defining product-related event types for Kafka communication.
 * Used by product-service to publish events and by order-service to consume them.
 */
@Getter
public enum ProductEventType {
    PRODUCT_VALIDATION_SUCCEEDED("product_validation_succeeded", "ProductValidationSucceededEvent", "product.validation.succeeded"),
    PRODUCT_VALIDATION_FAILED("product_validation_failed", "ProductValidationFailedEvent", "product.validation.failed"),
    PRODUCT_INVENTORY_UPDATED("product_inventory_updated", "ProductInventoryUpdatedEvent", "product.inventory.updated");

    private final String topic;
    private final String eventType;
    private final String subject;

    ProductEventType(String topic, String eventType, String subject) {
        this.topic = topic;
        this.eventType = eventType;
        this.subject = subject;
    }
}