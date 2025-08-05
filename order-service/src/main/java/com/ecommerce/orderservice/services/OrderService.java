package com.ecommerce.orderservice.services;

import com.ecommerce.orderservice.model.dto.OrderDto;
import com.ecommerce.orderservice.model.request.CreateOrderRequest;
import com.ecommerce.orderservice.model.request.UpdateOrderRequest;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface OrderService {
    OrderDto createOrder(CreateOrderRequest request);

    Page<OrderDto> getAllOrders(int page, int size);

    OrderDto getOrderById(UUID id);

    OrderDto updateOrder(UUID id, UpdateOrderRequest request);

    void cancelOrder(UUID id);
}
