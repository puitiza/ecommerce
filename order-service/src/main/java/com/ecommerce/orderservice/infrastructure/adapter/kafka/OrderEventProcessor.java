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
    private final KafkaTemplate<String, OrderEventPayload> kafkaTemplate;

    @Transactional
    public void processEvent(OrderEventPayload eventPayload, OrderEventType eventType) {
        Order orderFound = repositoryPort.findById(eventPayload.id());
        if (orderFound == null) {
            log.error("Order not found for ID: {}", eventPayload.id());
            return;
        }

        try {
            StateMachine<OrderStatus, OrderEventType> sm = stateMachineFactory.getStateMachine(orderFound.id().toString());
            persister.restore(sm, orderFound.id().toString());
            sm.startReactively().subscribe();

            // Defer VALIDATION_SUCCEEDED if not in VALIDATION_PENDING
            if (eventType == OrderEventType.VALIDATION_SUCCEEDED && orderFound.status() != OrderStatus.VALIDATION_PENDING) {
                log.warn("Received VALIDATION_SUCCEEDED for order ID: {} in state {}. Re-publishing in 5 seconds.",
                        orderFound.id(), orderFound.status());
                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
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
                    .setHeader("order", orderFound)
                    .build();

            sm.sendEvent(Mono.just(message))
                    .doOnNext(result -> {
                        try {
                            persister.persist(sm, orderFound.id().toString());
                            log.info("Processed event {} -> state {} for order ID: {}",
                                    eventType, sm.getState().getId(), orderFound.id());
                        } catch (Exception e) {
                            log.error("Failed to persist state for order ID: {}", orderFound.id(), e);
                        }
                    })
                    .doOnError(error -> log.error("Failed to send event {} to state machine for order {}: {}",
                            eventType, orderFound.id(), error.getMessage()))
                    .subscribe();
        } catch (Exception e) {
            log.error("Error processing event {} for order {}", eventType, orderFound.id(), e);
        }
    }
}