package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.model.Order;

/**
 * Domain service for handling order-related business logic.
 */
public interface OrderDomainService {

    /**
     * Checks if an order can be canceled.
     *
     * @param order The order to check.
     * @return True if the order can be canceled, false otherwise.
     */
    boolean canCancel(Order order);

    /**
     * Checks if an order can be updated.
     *
     * @param order The order to check.
     * @return True if the order can be updated, false otherwise.
     */
    boolean canUpdate(Order order);

    /**
     * Triggers the order creation event.
     *
     * @param order The order to process.
     */
    void sendCreateEvent(Order order);

    void sendUpdateEvent(Order order);

    void sendConfirmEvent(Order order);

    void sendEvent(Order order, OrderEventType orderEventType);

    /**
     * Triggers the order cancellation event.
     *
     * @param order The order to cancel.
     */
    void sendCancelEvent(Order order);
}