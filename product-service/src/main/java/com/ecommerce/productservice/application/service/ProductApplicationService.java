package com.ecommerce.productservice.application.service;

import com.ecommerce.productservice.domain.port.in.ProductUseCase;
import com.ecommerce.shared.domain.event.OrderEventPayload;

/**
 * Application service interface for product management, extending core use cases with event-driven logic.
 */
public interface ProductApplicationService extends ProductUseCase {

    void validateAndReserveStock(OrderEventPayload payload);

    void restock(OrderEventPayload payload);
}
