package com.ecommerce.orderservice.domain.port.out;

import com.ecommerce.shared.domain.event.OrderEventPayload;

public interface OrderEventPublisherPort {
    void publishOrderCreatedEvent(OrderEventPayload order);

    void publishOrderUpdatedEvent(OrderEventPayload order);

    void publishValidationSucceededEvent(OrderEventPayload order);

    void publishValidationFailedEvent(OrderEventPayload order);

    void publishRetryValidationEvent(OrderEventPayload order);

    void publishPaymentStartEvent(OrderEventPayload order);

    void publishPaymentSucceededEvent(OrderEventPayload order);

    void publishPaymentFailedEvent(OrderEventPayload order);

    void publishRetryPaymentEvent(OrderEventPayload order);

    void publishShipmentStartEvent(OrderEventPayload order);

    void publishShipmentSucceededEvent(OrderEventPayload order);

    void publishShipmentFailedEvent(OrderEventPayload order);

    void publishRetryShipmentEvent(OrderEventPayload order);

    void publishDeliveredEvent(OrderEventPayload order);

    void publishCancelEvent(OrderEventPayload order);
}