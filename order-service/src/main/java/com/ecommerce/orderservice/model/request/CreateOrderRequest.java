package com.ecommerce.orderservice.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@Schema(description = "Request object for creating a new order")
public class CreateOrderRequest {

    @NotNull(message = "User ID cannot be null")
    @Schema(description = "ID of the user placing the order")
    private String userId;

    //@NotBlank(message = "Items cannot be empty")
    @Schema(description = "List of items to order", example = "[{productId: 1, quantity: 2}, {productId: 4, quantity: 1}]")
    private List<OrderItemRequest> items;

    @NotBlank(message = "Shipping address is required")
    @Schema(description = "Shipping address for the order", example = "123 Main St, Anytown, CA 12345")
    private String shippingAddress;
}
