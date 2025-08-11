package com.ecommerce.orderservice.application.service;

import com.ecommerce.orderservice.application.dto.OrderResponse;
import com.ecommerce.orderservice.application.dto.OrderRequest;
import com.ecommerce.orderservice.application.dto.UpdateOrderRequest;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request);

    Page<OrderResponse> getAllOrders(int page, int size);

    OrderResponse getOrderById(UUID id);

    OrderResponse updateOrder(UUID id, UpdateOrderRequest request);

    void cancelOrder(UUID id);
}
