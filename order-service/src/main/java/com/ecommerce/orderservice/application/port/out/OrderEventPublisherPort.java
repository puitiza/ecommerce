package com.ecommerce.orderservice.application.port.out;

import com.ecommerce.orderservice.domain.model.Order;

public interface OrderEventPublisherPort {
    void publishOrderCreatedEvent(Order order);

    void publishOrderValidatedEvent(Order order);

    void publishValidationFailedEvent(Order order);

    void publishOrderUpdatedEvent(Order order);

    void publishOrderCancelledEvent(Order order);
}