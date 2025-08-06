package com.ecommerce.orderservice.application.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request object for an order item")
public record OrderItemRequest(
        @NotNull(message = "Product ID is required")
        @Schema(description = "ID of the product to order", example = "1")
        Long productId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        @Schema(description = "Quantity of the product to order", example = "2")
        Integer quantity
) {
}