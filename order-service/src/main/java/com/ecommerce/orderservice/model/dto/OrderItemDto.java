package com.ecommerce.orderservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto {

    private Long productId;

    private String productName; // Assuming you want to include product name in the response

    private Integer quantity;

    private Double unitPrice;

}
