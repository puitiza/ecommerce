package com.ecommerce.orderservice.application.port.in;

import com.ecommerce.orderservice.application.dto.OrderResponse;
import com.ecommerce.orderservice.application.dto.UpdateOrderRequest;

import java.util.UUID;

public interface UpdateOrderUseCase {
    OrderResponse updateOrder(UUID id, UpdateOrderRequest request);
}