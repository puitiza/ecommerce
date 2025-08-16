package com.ecommerce.orderservice.domain.port;

import com.ecommerce.orderservice.domain.model.Order;

public interface OrderEventPublisherPort {
    void publishOrderCreatedEvent(Order order);

    void publishOrderUpdatedEvent(Order order);

    void publishValidationSucceededEvent(Order order);

    void publishValidationFailedEvent(Order order);

    void publishRetryValidationEvent(Order order);

    void publishPaymentStartEvent(Order order);

    void publishPaymentSucceededEvent(Order order);

    void publishPaymentFailedEvent(Order order);

    void publishRetryPaymentEvent(Order order);

    void publishShipmentStartEvent(Order order);

    void publishShipmentSucceededEvent(Order order);

    void publishShipmentFailedEvent(Order order);

    void publishRetryShipmentEvent(Order order);

    void publishDeliveredEvent(Order order);

    void publishCancelEvent(Order order);

}