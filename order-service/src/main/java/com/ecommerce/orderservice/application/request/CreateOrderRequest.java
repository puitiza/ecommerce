package com.ecommerce.orderservice.application.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request object for creating a new order")
public record CreateOrderRequest(
        @NotNull(message = "The list of items cannot be null")
        @NotEmpty(message = "The list of items cannot be empty")
        @Schema(description = "List of items to order", example = "[{productId: 1, quantity: 2}, {productId: 4, quantity: 1}]")
        List<OrderItemRequest> items,

        @NotBlank(message = "Shipping address is required")
        @Schema(description = "Shipping address for the order", example = "123 Main St, Anytown, CA 12345")
        String shippingAddress,

        String userId
) {
    public CreateOrderRequest withUserId(String userId) {
        return new CreateOrderRequest(items, shippingAddress, userId);
    }
}