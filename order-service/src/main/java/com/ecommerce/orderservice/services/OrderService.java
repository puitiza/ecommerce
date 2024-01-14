package com.ecommerce.orderservice.services;

import com.ecommerce.orderservice.model.dto.OrderDto;
import com.ecommerce.orderservice.model.request.CreateOrderRequest;
import com.ecommerce.orderservice.model.request.UpdateOrderRequest;

import java.util.List;

public interface OrderService {
    OrderDto createOrder(CreateOrderRequest request);
    List<OrderDto> getAllOrders();
    OrderDto getOrderById(Long id);
    OrderDto updateOrder(Long id, UpdateOrderRequest request);
    void cancelOrder(Long id);
}
