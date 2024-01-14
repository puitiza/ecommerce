package com.ecommerce.orderservice.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrderItemRequest {

    @NotNull(message = "Product ID is required")
    @Schema(description = "ID of the product to order", example = "1")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    @Schema(description = "Quantity of the product to order", example = "2")
    private Integer quantity;
}
