package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.domain.model.Order;

public interface OrderDomainService {
    boolean canCancel(Order order);

    Order calculateTotalPrice(Order order);
}