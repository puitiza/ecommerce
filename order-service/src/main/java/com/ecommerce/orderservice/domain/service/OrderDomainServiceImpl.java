package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


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
    public void sendCreateEvent(Order order) {
        StateMachine<OrderStatus, OrderEventType> sm = stateMachineFactory.getStateMachine(order.id().toString());
        sm.startReactively().subscribe();

        Message<OrderEventType> message = MessageBuilder
                .withPayload(OrderEventType.ORDER_CREATED)
                .setHeader("order", order)
                .build();

        sm.sendEvent(Mono.just(message)).subscribe();
    }

    @Override
    public void sendCancelEvent(Order order) {
        StateMachine<OrderStatus, OrderEventType> sm = stateMachineFactory.getStateMachine(order.id().toString());
        Message<OrderEventType> message = MessageBuilder
                .withPayload(OrderEventType.CANCEL)
                .setHeader("order", order)
                .build();
        sm.sendEvent(Mono.just(message)).subscribe();
    }

    // Here you can add a method for the update event if you need it in the future,
    // But for now, the "update" flow is a "create" again.

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