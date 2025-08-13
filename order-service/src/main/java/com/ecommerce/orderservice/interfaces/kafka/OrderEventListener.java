package com.ecommerce.orderservice.interfaces.kafka;

import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import com.ecommerce.orderservice.domain.port.OrderRepositoryPort;
import io.cloudevents.CloudEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderRepositoryPort repositoryPort;
    private final StateMachineFactory<OrderStatus, OrderEventType> stateMachineFactory;

    @KafkaListener(topics = "validation_succeeded")
    public void handleValidationSucceeded(CloudEvent event) {
        Order order = extractOrderFromEvent(event);
        if (order == null) {
            log.warn("Null order in ValidationSucceeded event");
            return;
        }
        StateMachine<OrderStatus, OrderEventType> sm = stateMachineFactory.getStateMachine(order.id().toString());
        Message<OrderEventType> message = MessageBuilder
                .withPayload(OrderEventType.VALIDATION_SUCCEEDED)
                .setHeader("order", order)
                .build();
        sm.sendEvent(Mono.just(message)).subscribe();
        repositoryPort.save(order.withStatus(sm.getState().getId()));
    }

    @KafkaListener(topics = "validation_failed")
    public void handleValidationFailed(CloudEvent event) {
        Order order = extractOrderFromEvent(event);
        if (order == null) {
            log.warn("Null order in ValidationFailed event");
            return;
        }
        StateMachine<OrderStatus, OrderEventType> sm = stateMachineFactory.getStateMachine(order.id().toString());
        Message<OrderEventType> message = MessageBuilder
                .withPayload(OrderEventType.VALIDATION_FAILED)
                .setHeader("order", order)
                .build();
        sm.sendEvent(Mono.just(message)).subscribe();
        repositoryPort.save(order.withStatus(sm.getState().getId()));
    }

    // Add similar for other events
    // extractOrderFromEvent: Implement to parse Order from event data
}
