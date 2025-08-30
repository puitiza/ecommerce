package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDomainServiceImpl implements OrderDomainService {

    private final StateMachineFactory<OrderStatus, OrderEventType> stateMachineFactory;
    private final StateMachinePersister<OrderStatus, OrderEventType, String> persister;

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

    @Override
    @Transactional
    public void sendEvent(Order order, OrderEventType eventType) {
        try {
            StateMachine<OrderStatus, OrderEventType> sm = stateMachineFactory.getStateMachine(order.id().toString());
            persister.restore(sm, order.id().toString());
            sm.startReactively().subscribe();

            Message<OrderEventType> message = MessageBuilder
                    .withPayload(eventType)
                    .setHeader("order", order)
                    .build();

            sm.sendEvent(Mono.just(message))
                    .doOnNext(result -> {
                        try {
                            persister.persist(sm, order.id().toString());
                            log.info("Sent event {} for order ID: {}, new state: {}",
                                    eventType, order.id(), sm.getState().getId());
                        } catch (Exception e) {
                            log.error("Failed to persist state for order {}", order.id(), e);
                        }
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Error handling event {} for order {}", eventType, order.id(), e);
            throw new RuntimeException(e);
        }
    }

    private boolean isEventAllowed(Order order, OrderEventType eventType) {
        try {
            StateMachine<OrderStatus, OrderEventType> sm = stateMachineFactory.getStateMachine(order.id().toString());
            persister.restore(sm, order.id().toString());
            sm.startReactively().subscribe();

            return sm.getTransitions()
                    .stream()
                    .anyMatch(transition ->
                            transition.getSource().getId().equals(order.status()) &&
                                    transition.getTrigger() != null &&
                                    transition.getTrigger().getEvent().equals(eventType));
        } catch (Exception e) {
            log.error("Error checking if event {} is allowed for order {}", eventType, order.id(), e);
            return false;
        }
    }
}