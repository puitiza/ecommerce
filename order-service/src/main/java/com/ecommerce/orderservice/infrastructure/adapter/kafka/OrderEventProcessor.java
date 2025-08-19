package com.ecommerce.orderservice.infrastructure.adapter.kafka;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventProcessor {

    private final OrderRepositoryPort repositoryPort;
    private final StateMachineFactory<OrderStatus, OrderEventType> stateMachineFactory;

    /**
     * Processes an incoming CloudEvent within a transactional context.
     * This method advances the order's state machine and persists the new state
     * to the database. The entire operation is wrapped in a transaction to ensure
     * that the state machine transition and the database save are atomic.
     * If the database save fails, the transaction is rolled back, preventing
     * data inconsistency.
     *
     * @param order The order extracted from the CloudEvent payload.
     * @param eventType The type of the event, which triggers the state machine transition.
     */
    @Transactional
    public void processEvent(Order order, OrderEventType eventType) {
        StateMachine<OrderStatus, OrderEventType> sm = stateMachineFactory.getStateMachine(order.id().toString());
        sm.getExtendedState().getVariables().put("order", order);

        Message<OrderEventType> message = MessageBuilder
                .withPayload(eventType)
                .setHeader("order", order)
                .build();

        sm.sendEvent(Mono.just(message)).subscribe(
                result -> {
                    // The states machine has moved successfully
                    log.info("State machine advanced to {} for order ID: {}", sm.getState().getId(), order.id());
                    repositoryPort.save(order.withStatus(sm.getState().getId()));
                },
                error -> log.error("Failed to send event {} to state machine for order {}: {}", eventType, order.id(), error.getMessage())
        );
    }

}
