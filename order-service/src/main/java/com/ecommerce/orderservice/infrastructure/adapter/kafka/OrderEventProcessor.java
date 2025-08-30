package com.ecommerce.orderservice.infrastructure.adapter.kafka;

import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import com.ecommerce.orderservice.domain.port.out.OrderRepositoryPort;
import com.ecommerce.shared.domain.event.OrderEventPayload;
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
public class OrderEventProcessor {

    private final OrderRepositoryPort repositoryPort;
    private final StateMachineFactory<OrderStatus, OrderEventType> stateMachineFactory;
    private final StateMachinePersister<OrderStatus, OrderEventType, String> persister;

    /**
     * Processes any incoming order event by finding the order and applying the event to the state machine.
     *
     * @param eventPayload The event payload.
     * @param eventType    The type of event to apply.
     */
    @Transactional
    public void processEvent(OrderEventPayload eventPayload, OrderEventType eventType) {
        Order orderFound = repositoryPort.findById(eventPayload.id());
        applyEventToStateMachine(orderFound, eventType, eventPayload);
    }

    /**
     * Handles the auto-validate event, checking the order's status before
     * proceeding with the validation flow.
     *
     * @param eventPayload The event payload containing the order ID.
     */
    @Transactional
    public void processAutoValidateEvent(OrderEventPayload eventPayload) {
        Order orderFound = repositoryPort.findById(eventPayload.id());


        log.info("Order ID {} is in CREATED state. Proceeding with validation.", orderFound.id());
        applyEventToStateMachine(orderFound, OrderEventType.ORDER_CREATED, eventPayload);

    }

    /**
     * Reusable method to restore the state machine, apply an event, and persist the new state.
     *
     * @param order        The order entity.
     * @param eventType    The event to send to the state machine.
     * @param eventPayload The event payload.
     */
    private void applyEventToStateMachine(Order order, OrderEventType eventType, OrderEventPayload eventPayload) {
        try {
            StateMachine<OrderStatus, OrderEventType> sm = stateMachineFactory.getStateMachine(order.id().toString());
            persister.restore(sm, order.id().toString());
            sm.startReactively().subscribe();

            sm.getExtendedState().getVariables().put("order", eventPayload);

            Message<OrderEventType> message = MessageBuilder
                    .withPayload(eventType)
                    .setHeader("order", order)
                    .build();

            sm.sendEvent(Mono.just(message))
                    .doOnNext(result -> {
                        try {
                            persister.persist(sm, order.id().toString());
                            log.info("Processed event {} -> state {} for order ID: {}",
                                    eventType, sm.getState().getId(), order.id());
                        } catch (Exception e) {
                            log.error("Failed to persist state for order ID: {}", order.id(), e);
                        }
                    })
                    .doOnError(error -> log.error("Failed to send event {} to state machine for order {}: {}",
                            eventType, order.id(), error.getMessage()))
                    .subscribe();
        } catch (Exception e) {
            log.error("Error applying event {} to state machine for order {}", eventType, order.id(), e);
        }
    }
}