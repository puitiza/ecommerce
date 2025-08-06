package com.ecommerce.orderservice.application.port.in;

import com.ecommerce.orderservice.application.dto.OrderResponse;
import org.springframework.data.domain.Page;

public interface GetAllOrdersUseCase {
    Page<OrderResponse> getAllOrders(int page, int size);
}