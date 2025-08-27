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

import java.util.UUID;

/**
 * Persists and retrieves order state machine states using the order repository.
 */
@Component
@RequiredArgsConstructor
public class OrderStateMachinePersister implements StateMachinePersist<OrderStatus, OrderEventType, String> {

    private final OrderRepositoryPort orderRepositoryPort;

    /**
     * Persists the state machine context by updating the order's status in the repository.
     *
     * @param context the state machine context
     * @param orderId the order ID
     */
    @Override
    public void write(StateMachineContext<OrderStatus, OrderEventType> context, String orderId) {
        Order order = orderRepositoryPort.findById(UUID.fromString(orderId));
        if (order != null) {
            Order updatedOrder = order.withStatus(context.getState());
            orderRepositoryPort.save(updatedOrder);
        }
    }

    /**
     * Retrieves the state machine context for an order by its ID.
     *
     * @param orderId the order ID
     * @return the state machine context, or null if the order is not found
     */
    @Override
    public StateMachineContext<OrderStatus, OrderEventType> read(String orderId) {
        Order order = orderRepositoryPort.findById(UUID.fromString(orderId));
        if (order != null) {
            return new DefaultStateMachineContext<>(order.status(), null, null, null, null);
        }
        return null;
    }
}