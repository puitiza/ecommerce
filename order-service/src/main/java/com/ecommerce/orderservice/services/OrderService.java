package com.ecommerce.orderservice.services;

import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderWithProducts;

import java.util.List;

public interface OrderService {
    Order getOrderById(Long orderId);

    List<Order> getAllOrders();
    OrderWithProducts getOrderWithProducts(Long orderId);
}
