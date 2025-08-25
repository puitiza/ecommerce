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

/**
 * Implementation of the OrderDomainService, handling state machine interactions for order processing.
 */
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
        sendEvent(order, OrderEventType.ORDER_CREATED);
    }

    @Override
    public void sendUpdateEvent(Order order) {
        sendEvent(order, OrderEventType.ORDER_UPDATED);
    }

    @Override
    public void sendConfirmEvent(Order order) {
        sendEvent(order, OrderEventType.ORDER_CREATED); // Same as create to trigger validation
    }

    @Override
    public void sendCancelEvent(Order order) {
        sendEvent(order, OrderEventType.CANCEL);
    }

    private void sendEvent(Order order, OrderEventType eventType) {
        StateMachine<OrderStatus, OrderEventType> sm = stateMachineFactory.getStateMachine(order.id().toString());
        sm.startReactively().subscribe();
        Message<OrderEventType> message = MessageBuilder
                .withPayload(eventType)
                .setHeader("order", order)
                .build();
        sm.sendEvent(Mono.just(message)).subscribe();
    }

    /**
     * Checks if a given event is allowed for the current state of an order.
     * This is an important part of the domain logic.
     *
     * @param order     The order to check.
     * @param eventType The event to check.
     * @return True if the transition is allowed, false otherwise.
     */
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