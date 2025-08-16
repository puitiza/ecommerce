package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.application.dto.OrderItemResponse;
import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Service
@RequiredArgsConstructor
public class OrderDomainServiceImpl implements OrderDomainService {

    private final StateMachineFactory<OrderStatus, OrderEventType> stateMachineFactory;

    @Override
    public boolean canCancel(Order order) {
        return isEventAllowed(order, OrderEventType.CANCEL);
    }

    @Override
    public boolean canUpdate(Order order) {
        return isEventAllowed(order, OrderEventType.ORDER_UPDATED);
    }

    @Override
    public BigDecimal calculateTotalPrice(List<OrderItemResponse> items) {
        return items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private boolean isEventAllowed(Order order, OrderEventType eventType) {
        StateMachine<OrderStatus, OrderEventType> sm = stateMachineFactory.getStateMachine(order.id().toString());
        sm.startReactively().subscribe(); // ensures that the machine is in its initial state

        // Verify if there is a transition with the state of origin of the order and the event
        return sm.getTransitions()
                .stream()
                .anyMatch(transition ->
                        transition.getSource().getId().equals(order.status()) &&
                                transition.getTrigger() != null &&
                                transition.getTrigger().getEvent().equals(eventType));
    }
}