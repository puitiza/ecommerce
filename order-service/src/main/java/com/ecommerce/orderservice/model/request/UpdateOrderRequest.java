package com.ecommerce.orderservice.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class UpdateOrderRequest {

    @NotEmpty(message = "Items cannot be empty")
    @Schema(description = "List of items to update quantities for (optional)", example = "[{productId: 4, quantity: 3}]")
    private List<OrderItemRequest> items; // Only for updating quantities

    @Schema(description = "Updated shipping address (optional)", example = "456 Elm St, Anytown, CA 54321")
    private String shippingAddress;
}
