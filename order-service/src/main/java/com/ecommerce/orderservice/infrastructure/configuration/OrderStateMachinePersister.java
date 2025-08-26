package com.ecommerce.orderservice.infrastructure.configuration;

import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import com.ecommerce.orderservice.domain.port.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderStateMachinePersister implements StateMachinePersist<OrderStatus, OrderEventType, String> {

    private final OrderRepositoryPort orderRepositoryPort;

    @Override
    public void write(StateMachineContext<OrderStatus, OrderEventType> context, String contextObj) {
        Order order = orderRepositoryPort.findById(java.util.UUID.fromString(contextObj));
        if (order != null) {
            Order updatedOrder = order.withStatus(context.getState());
            orderRepositoryPort.save(updatedOrder);
        }
    }

    @Override
    public StateMachineContext<OrderStatus, OrderEventType> read(String contextObj) {
        Order order = orderRepositoryPort.findById(java.util.UUID.fromString(contextObj));
        if (order != null) {
            return new DefaultStateMachineContext<>(order.status(), null, null, null, null);
        }
        return null;
    }
}