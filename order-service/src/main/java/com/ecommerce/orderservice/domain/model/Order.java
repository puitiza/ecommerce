package com.ecommerce.orderservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record Order(
        UUID id,
        String userId,
        Set<OrderItem> items,
        OrderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        BigDecimal totalPrice,
        String shippingAddress
) {
    public Order withStatus(OrderStatus status) {
        return new Order(id, userId, items, status, createdAt, updatedAt, totalPrice, shippingAddress);
    }

    public Order withItemsAndTotalPrice(Set<OrderItem> items, BigDecimal totalPrice) {
        return new Order(id, userId, items, status, createdAt, updatedAt, totalPrice, shippingAddress);
    }
}