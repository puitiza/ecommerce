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

@Getter
@Setter
class OrderItemDto {

    private Long productId;

    private String productName; // Assuming you want to include product name in the response

    private Integer quantity;

    private BigDecimal unitPrice;

}

