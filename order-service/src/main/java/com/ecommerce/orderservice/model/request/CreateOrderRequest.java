package com.ecommerce.orderservice.model.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@Getter
@Setter
@ToString
@Schema(description = "Request object for creating a new order")
public class CreateOrderRequest {

    @JsonIgnore
    private String userId;

    //@NotBlank(message = "Items cannot be empty")
    @Schema(description = "List of items to order", example = "[{productId: 1, quantity: 2}, {productId: 4, quantity: 1}]")
    private List<OrderItemRequest> items;

    @NotBlank(message = "Shipping address is required")
    @Schema(description = "Shipping address for the order", example = "123 Main St, Anytown, CA 12345")
    private String shippingAddress;

    public void extractUserIdFromToken(String sub) {
        this.userId = sub;
    }
}
