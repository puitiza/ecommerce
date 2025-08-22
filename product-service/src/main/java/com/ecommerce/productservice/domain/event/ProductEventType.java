package com.ecommerce.productservice.domain.event;

import lombok.Getter;

@Getter
public enum ProductEventType {
    PRODUCT_VALIDATION_SUCCEEDED("product.validation.succeeded", "product_validation_succeeded"),
    PRODUCT_VALIDATION_FAILED("product.validation.failed", "product_validation_failed"),
    PRODUCT_INVENTORY_UPDATED("product.inventory.updated", "product_inventory_updated");

    private final String eventType;
    private final String topic;

    ProductEventType(String eventType, String topic) {
        this.eventType = eventType;
        this.topic = topic;
    }

}