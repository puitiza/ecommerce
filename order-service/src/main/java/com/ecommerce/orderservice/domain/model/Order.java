package com.ecommerce.orderservice.domain.model;

import com.ecommerce.orderservice.application.dto.OrderRequest;
import com.ecommerce.shared.domain.event.OrderEventPayload;
import com.ecommerce.shared.domain.event.OrderEventPayload.OrderItemPayload;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    /**
     * Updates an order with new items, total price, and shipping address.
     *
     * @param request       The order update request.
     * @param itemsReplaced The new set of order items.
     * @param totalUpdated  The new total price.
     * @return A new, updated Order instance.
     */
    public Order updateFrom(OrderRequest request, Set<OrderItem> itemsReplaced, BigDecimal totalUpdated) {
        return new Order(
                id,
                userId,
                itemsReplaced,
                OrderStatus.CREATED,
                createdAt,
                LocalDateTime.now(),
                totalUpdated,
                request.shippingAddress() != null ? request.shippingAddress() : shippingAddress
        );
    }

    public OrderEventPayload toEventPayload() {
        return new OrderEventPayload(
                id,
                items.stream()
                        .map(item -> new OrderItemPayload(item.productId(), item.quantity()))
                        .collect(Collectors.toSet())
        );
    }

}