package com.ecommerce.orderservice.domain.model;

import java.math.BigDecimal;

/**
 * OrderItem represents a value object within the Order aggregate.
 * It is a component of the Order and does not have its own lifecycle.
 */
public record OrderItem(
        Long id,
        Long productId,
        Integer quantity,
        BigDecimal unitPrice
) {}