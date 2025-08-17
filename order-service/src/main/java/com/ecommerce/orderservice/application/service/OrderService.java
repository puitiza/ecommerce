package com.ecommerce.orderservice.application.service;

import com.ecommerce.orderservice.application.dto.OrderRequest;
import com.ecommerce.orderservice.application.dto.OrderResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Defines the use cases for managing orders.
 * This interface is the primary entry point for all order-related operations
 * in the application layer.
 */
public interface OrderService {
    OrderResponse createOrder(OrderRequest request);

    Page<OrderResponse> getAllOrders(int page, int size);

    OrderResponse getOrderById(UUID id);

    OrderResponse updateOrder(UUID id, OrderRequest request);

    void deleteOrder(UUID id);

    void cancelOrder(UUID id);
}
