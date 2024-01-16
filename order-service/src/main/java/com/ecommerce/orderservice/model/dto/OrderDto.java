package com.ecommerce.orderservice.model.dto;

import com.ecommerce.orderservice.model.entity.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderDto {

    private Long id;

    private Long userId;

    private List<OrderItemDto> items;

    private OrderStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private BigDecimal totalPrice;

    private String shippingAddress;

}



