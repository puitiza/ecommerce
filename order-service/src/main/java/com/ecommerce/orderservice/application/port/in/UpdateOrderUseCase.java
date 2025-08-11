package com.ecommerce.orderservice.application.port.in;

import com.ecommerce.orderservice.application.dto.OrderRequest;
import com.ecommerce.orderservice.application.dto.OrderResponse;

import java.util.UUID;

public interface UpdateOrderUseCase {
    OrderResponse updateOrder(UUID id, OrderRequest request);
}