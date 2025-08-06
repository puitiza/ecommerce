package com.ecommerce.orderservice.application.dto;

import com.ecommerce.orderservice.domain.model.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Represents an order response")
public record OrderResponse(
        @Schema(description = "Unique identifier for the order", example = "98765432-1098-cdef-0123-456789012345")
        String id,
        @Schema(description = "Identifier of the user who placed the order", example = "a5a6d1e9-cc05-4614-b83a-104d17b92d10")
        String userId,
        @Schema(description = "List of items included in the order")
        List<OrderItemResponse> items,
        @Schema(description = "Current status of the order", example = "VALIDATION_SUCCEEDED")
        OrderStatus status,
        @Schema(description = "Date and time when the order was created", example = "2024-01-19T00:18:54.343")
        LocalDateTime createdAt,
        @Schema(description = "Date and time when the order was last updated", example = "2024-01-19T00:18:54.367")
        LocalDateTime updatedAt,
        @Schema(description = "Total price of the order", example = "75.94")
        BigDecimal totalPrice,
        @Schema(description = "Shipping address for the order", example = "123 Main St, Anytown, CA 12345")
        String shippingAddress
) {}