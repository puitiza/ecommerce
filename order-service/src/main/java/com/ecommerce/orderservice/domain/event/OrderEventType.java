package com.ecommerce.orderservice.domain.event;

import lombok.Getter;

@Getter
public enum OrderEventType {

    ORDER_CREATED("order_created", "OrderCreatedEvent", "order.created"),
    ORDER_UPDATED("order_updated", "OrderUpdatedEvent", "order.updated"),
    VALIDATION_SUCCEEDED("validation_succeeded", "ValidationSucceededEvent", "validation.succeeded"),
    VALIDATION_FAILED("validation_failed", "ValidationFailedEvent", "validation.failed"),
    RETRY_VALIDATION("retry_validation", "RetryValidationEvent", "retry.validation"),
    PAYMENT_START("payment_start", "PaymentStartEvent", "payment.start"),
    PAYMENT_SUCCEEDED("payment_succeeded", "PaymentSucceededEvent", "payment.succeeded"),
    PAYMENT_FAILED("payment_failed", "PaymentFailedEvent", "payment.failed"),
    RETRY_PAYMENT("retry_payment", "RetryPaymentEvent", "retry.payment"),
    SHIPMENT_START("shipment_start", "ShipmentStartEvent", "shipment.start"),
    SHIPMENT_SUCCEEDED("shipment_succeeded", "ShipmentSucceededEvent", "shipment.succeeded"),
    SHIPMENT_FAILED("shipment_failed", "ShipmentFailedEvent", "shipment.failed"),
    RETRY_SHIPMENT("retry_shipment", "RetryShipmentEvent", "retry.shipment"),
    DELIVERED("delivered", "DeliveredEvent", "order.delivered"),
    CANCEL("cancel", "CancelEvent", "order.cancel");

    private final String topic;
    private final String eventType;
    private final String subject;

    OrderEventType(String topic, String eventType, String subject) {
        this.topic = topic;
        this.eventType = eventType;
        this.subject = subject;
    }
}