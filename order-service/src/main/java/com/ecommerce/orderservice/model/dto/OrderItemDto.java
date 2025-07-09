package com.ecommerce.orderservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Represents an item within an order.")
public class OrderItemDto {

    @Schema(description = "Unique identifier of the product", example = "1")
    private Long productId;

    @Schema(description = "Name of the product", example = "New Product Watch")
    private String productName;

    @Schema(description = "Quantity of the product ordered", example = "5")
    private Integer quantity;

    @Schema(description = "Unit price of the product", example = "10.99")
    private Double unitPrice;

}
