package com.ecommerce.orderservice.application.port.in;

import com.ecommerce.orderservice.application.dto.OrderResponse;
import com.ecommerce.orderservice.application.request.UpdateOrderRequest;

import java.util.UUID;

public interface UpdateOrderUseCase {
    OrderResponse updateOrder(UUID id, UpdateOrderRequest request);
}