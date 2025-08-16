package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.domain.model.Order;

public interface OrderDomainService {
    boolean canCancel(Order order);

    boolean canUpdate(Order order);

    void sendCreateEvent(Order order);

    void sendCancelEvent(Order order);
}