package com.ecommerce.orderservice.services;

import com.ecommerce.orderservice.model.Order;

import java.util.List;

public interface OrderService {
    Order getOrderById(Long orderId);

    List<Order> getAllOrders();
}
