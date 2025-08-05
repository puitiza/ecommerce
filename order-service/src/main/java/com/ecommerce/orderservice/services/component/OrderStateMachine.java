package com.ecommerce.orderservice.services.component;

import com.ecommerce.orderservice.infrastructure.persistence.entity.OrderEntity;
import com.ecommerce.orderservice.model.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class OrderStateMachine {
    private static final Set<OrderStatus> CANCELLABLE_STATES = Set.of(
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.PROCESSING
    );

    public boolean canCancel(OrderEntity orderEntity) {
        return CANCELLABLE_STATES.contains(orderEntity.getStatus());
    }
}
