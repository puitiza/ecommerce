package com.ecommerce.shared.domain.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;

/**
 * Minimal Payload for order-related events, shared between order-service and product-service.
 * Contains minimal data required for inventory validation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderEventPayload(
        UUID id,
        List<OrderItemPayload> items
) {
    public record OrderItemPayload(
            Long productId,
            Integer quantity
    ) {
    }
}