package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.application.dto.OrderItemResponse;
import com.ecommerce.orderservice.domain.model.Order;

import java.math.BigDecimal;
import java.util.List;

public interface OrderDomainService {
    boolean canCancel(Order order);

    boolean canUpdate(Order order);

    BigDecimal calculateTotalPrice(List<OrderItemResponse> itemResponses);
}