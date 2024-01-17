package com.ecommerce.orderservice.services;

import com.ecommerce.orderservice.model.dto.OrderDto;
import com.ecommerce.orderservice.model.request.CreateOrderRequest;
import com.ecommerce.orderservice.model.request.UpdateOrderRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {
    OrderDto createOrder(CreateOrderRequest request);
    Page<OrderDto> getAllOrders(int page, int size);
    OrderDto getOrderById(Long id);
    OrderDto updateOrder(Long id, UpdateOrderRequest request);
    void cancelOrder(Long id);
}
