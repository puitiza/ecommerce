package com.ecommerce.orderservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Represents the Order aggregate root in the domain model.
 * This record is immutable and encapsulates all core business logic and state transitions for an order.
 *
 * @version 1.0
 */
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

    /**
     * Creates a new order with an updated status.
     *
     * @param newStatus The new status of the order.
     * @return A new Order instance with the updated status and current timestamp.
     */
    public Order withStatus(OrderStatus newStatus) {
        return new Order(id, userId, items, newStatus, createdAt, LocalDateTime.now(), totalPrice, shippingAddress);
    }

}