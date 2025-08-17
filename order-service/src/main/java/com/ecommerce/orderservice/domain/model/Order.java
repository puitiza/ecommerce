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
     * Creates a new Order with updated status and timestamp.
     *
     * @param newStatus The new order status.
     * @return Updated Order instance.
     */
    public Order withStatus(OrderStatus newStatus) {
        return new Order(id, userId, items, newStatus, createdAt, LocalDateTime.now(), totalPrice, shippingAddress);
    }

}