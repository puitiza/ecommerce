package com.ecommerce.productservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request object for an order item")
public record BatchProductItemRequest(
        @NotNull(message = "Product ID is required")
        @Schema(description = "ID of the product", example = "1")
        Long productId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        @Schema(description = "Quantity of the product", example = "2")
        Integer quantity
) {
}