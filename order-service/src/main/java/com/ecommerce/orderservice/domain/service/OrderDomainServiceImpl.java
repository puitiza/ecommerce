package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import com.ecommerce.orderservice.domain.port.out.OrderRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

/**
 * Implementation of the OrderDomainService, handling state machine interactions for order processing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDomainServiceImpl implements OrderDomainService {

    private final StateMachineFactory<OrderStatus, OrderEventType> stateMachineFactory;
    private final OrderRepositoryPort orderRepositoryPort;

    @Override
    public boolean canCancel(Order order) {
        return isEventAllowed(order, OrderEventType.CANCEL);
    }

    @Override
    public boolean canUpdate(Order order) {
        return isEventAllowed(order, OrderEventType.ORDER_UPDATED);
    }

    @Override
    @Transactional
    public void sendCreateEvent(Order order) {
        sendEvent(order, OrderEventType.ORDER_CREATED);
    }

    @Override
    @Transactional
    public void sendUpdateEvent(Order order) {
        sendEvent(order, OrderEventType.ORDER_UPDATED);
    }

    @Override
    @Transactional
    public void sendConfirmEvent(Order order) {
        sendEvent(order, OrderEventType.ORDER_CREATED);
    }

    @Override
    @Transactional
    public void sendCancelEvent(Order order) {
        sendEvent(order, OrderEventType.CANCEL);
    }

    @Transactional
    protected void sendEvent(Order order, OrderEventType eventType) {
        log.info("Sending event {} for order ID: {}, current status: {}",
                eventType, order.id(), order.status());

        StateMachine<OrderStatus, OrderEventType> sm = stateMachineFactory.getStateMachine(order.id().toString());
        sm.getStateMachineAccessor().doWithAllRegions(accessor ->
                accessor.resetStateMachineReactively(new DefaultStateMachineContext<>(order.status(), null, null, null)).subscribe()
        );
        sm.startReactively().subscribe();

        Message<OrderEventType> message = MessageBuilder
                .withPayload(eventType)
                .setHeader("order", order)
                .build();

        sm.sendEvent(Mono.just(message))
                .doOnNext(result -> {
                    if (sm.getState() == null) {
                        log.error("State machine failed to transition for event {} on order {}. Current status: {}",
                                eventType, order.id(), order.status());
                        throw new IllegalStateException("State machine transition failed for event " + eventType);
                    }
                    Order updatedOrder = order.withStatus(sm.getState().getId());
                    orderRepositoryPort.save(updatedOrder);
                    log.info("Sent event {} for order ID: {}, new state: {}", eventType, order.id(), sm.getState().getId());
                })
                .doOnError(error -> log.error("Failed to send event {} for order {}: {}",
                        eventType, order.id(), error.getMessage()))
                .subscribe();
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
        sm.getStateMachineAccessor().doWithAllRegions(accessor ->
                accessor.resetStateMachineReactively(new DefaultStateMachineContext<>(order.status(), null, null, null)).subscribe()
        );
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