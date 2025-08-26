package com.ecommerce.orderservice.infrastructure.adapter.kafka;

import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import com.ecommerce.orderservice.domain.port.out.OrderRepositoryPort;
import com.ecommerce.shared.domain.event.OrderEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventProcessor {

    private final OrderRepositoryPort repositoryPort;
    private final StateMachineFactory<OrderStatus, OrderEventType> stateMachineFactory;
    private final KafkaTemplate<String, OrderEventPayload> kafkaTemplate;

    /**
     * Processes an incoming CloudEvent within a transactional context.
     * This method advances the order's state machine and persists the new state
     * to the database. The entire operation is wrapped in a transaction to ensure
     * that the state machine transition and the database save are atomic.
     * If the database save fails, the transaction is rolled back, preventing
     * data inconsistency.
     *
     * @param eventPayload The order extracted from the CloudEvent payload.
     * @param eventType    The type of the event, which triggers the state machine transition.
     */
    @Transactional
    public void processEvent(OrderEventPayload eventPayload, OrderEventType eventType) {
        Order orderFound = repositoryPort.findById(eventPayload.id());
        if (orderFound == null) {
            log.error("Order not found for ID: {}", eventPayload.id());
            return;
        }

        log.info("Processing event {} for order ID: {}, current status: {}",
                eventType, orderFound.id(), orderFound.status());

        // Create and restore state machine to current order status
        StateMachine<OrderStatus, OrderEventType> sm = stateMachineFactory.getStateMachine(orderFound.id().toString());
        sm.getStateMachineAccessor().doWithAllRegions(accessor ->
                accessor.resetStateMachineReactively(new DefaultStateMachineContext<>(orderFound.status(), null, null, null)).subscribe()
        );
        sm.startReactively().subscribe();

        // Defer VALIDATION_SUCCEEDED if not in VALIDATION_PENDING
        if (eventType == OrderEventType.VALIDATION_SUCCEEDED && orderFound.status() != OrderStatus.VALIDATION_PENDING) {
            log.warn("Received VALIDATION_SUCCEEDED for order ID: {} in state {}. Re-publishing in 5 seconds.",
                    orderFound.id(), orderFound.status());
            // Re-publish to Kafka with a delay
            new Thread(() -> {
                try {
                    Thread.sleep(5000); // Wait 5 seconds
                    kafkaTemplate.send(eventType.getTopic(), eventPayload.id().toString(), eventPayload);
                    log.info("Re-published VALIDATION_SUCCEEDED for order ID: {}", eventPayload.id());
                } catch (InterruptedException e) {
                    log.error("Failed to re-publish VALIDATION_SUCCEEDED for order ID: {}", eventPayload.id(), e);
                }
            }).start();
            return;
        }

        sm.getExtendedState().getVariables().put("order", eventPayload);

        Message<OrderEventType> message = MessageBuilder
                .withPayload(eventType)
                .setHeader("order", eventPayload)
                .build();

        sm.sendEvent(Mono.just(message))
                .doOnNext(result -> {
                    if (sm.getState() == null) {
                        log.error("State machine failed to transition for event {} on order {}. Current status: {}",
                                eventType, orderFound.id(), orderFound.status());
                        return;
                    }
                    Order updatedOrder = orderFound.withStatus(sm.getState().getId());
                    repositoryPort.save(updatedOrder);
                    log.info("State machine advanced to {} for order ID: {}", sm.getState().getId(), orderFound.id());
                })
                .doOnError(error -> log.error("Failed to send event {} to state machine for order {}: {}",
                        eventType, orderFound.id(), error.getMessage()))
                .subscribe();
    }
}