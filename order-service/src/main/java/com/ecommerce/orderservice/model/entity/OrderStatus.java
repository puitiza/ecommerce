package com.ecommerce.orderservice.model.entity;

public enum OrderStatus {
    VALIDATING,
    PENDING,
    PROCESSING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
