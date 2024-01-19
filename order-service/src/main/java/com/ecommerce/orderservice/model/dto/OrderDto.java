package com.ecommerce.orderservice.model.dto;

import com.ecommerce.orderservice.model.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private Long id;

    private String userId;

    private List<OrderItemDto> items;

    private OrderStatus status;

    private String createdAt;

    private String updatedAt;

    private BigDecimal totalPrice;

    private String shippingAddress;

}



