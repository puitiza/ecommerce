package com.ecommerce.orderservice.application.port.in;

import java.util.UUID;

public interface CancelOrderUseCase {
    void cancelOrder(UUID id);
}