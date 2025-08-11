package com.ecommerce.orderservice.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Request object for updating an order")
public record UpdateOrderRequest(
        @NotEmpty(message = "Items cannot be empty")
        @Schema(description = "List of items to update quantities for", example = "[{productId: 4, quantity: 3}]")
        List<OrderItemRequest> items,

        @Schema(description = "Updated shipping address (optional)", example = "456 Elm St, Anytown, CA 54321")
        String shippingAddress
) {}