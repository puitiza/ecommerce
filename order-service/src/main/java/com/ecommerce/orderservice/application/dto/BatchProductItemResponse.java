package com.ecommerce.orderservice.application.dto;

import java.math.BigDecimal;

/**
 * DTO for product item details response from the Product Service.
 * Deserialization resilience is handled by ObjectMapper configuration.
 */
public record BatchProductItemResponse(
        Long productId,
        String name,
        BigDecimal price,
        boolean isAvailable,
        Integer availableUnits,
        String error
) {
}
