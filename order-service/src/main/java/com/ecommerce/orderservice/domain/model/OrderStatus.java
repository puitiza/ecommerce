package com.ecommerce.orderservice.domain.model;

public enum OrderStatus {
    CREATED,
    VALIDATING,
    VALIDATION_FAILED,
    VALIDATION_SUCCEEDED,
    PENDING,
    PROCESSING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}