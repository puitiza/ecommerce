package com.ecommerce.orderservice.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Represents an item within an order")
public record OrderItemResponse(
        @Schema(description = "Unique identifier of the product", example = "1")
        Long productId,
        @Schema(description = "Name of the product", example = "New Product Watch")
        String productName,
        @Schema(description = "Quantity of the product ordered", example = "5")
        Integer quantity,
        @Schema(description = "Unit price of the product", example = "10.99")
        BigDecimal unitPrice
) {}