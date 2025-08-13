package com.ecommerce.orderservice.interfaces.kafka;

import com.ecommerce.orderservice.domain.event.OrderEvent;
import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import com.ecommerce.orderservice.domain.port.OrderRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class OrderEventListener {

    private final OrderRepositoryPort repositoryPort;
    private final StateMachineFactory<OrderStatus, OrderEventType> stateMachineFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderEventListener(OrderRepositoryPort repositoryPort, StateMachineFactory<OrderStatus, OrderEventType> stateMachineFactory) {
        this.repositoryPort = repositoryPort;
        this.stateMachineFactory = stateMachineFactory;
        JavaTimeModule module = new JavaTimeModule();
        module.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")));
        objectMapper.registerModule(module);
    }

    @KafkaListener(topics = "validation_succeeded")
    public void handleValidationSucceeded(CloudEvent event) {
        processEvent(event, OrderEventType.VALIDATION_SUCCEEDED);
    }

    @KafkaListener(topics = "validation_failed")
    public void handleValidationFailed(CloudEvent event) {
        processEvent(event, OrderEventType.VALIDATION_FAILED);
    }

    @KafkaListener(topics = "payment_succeeded")
    public void handlePaymentSucceeded(CloudEvent event) {
        processEvent(event, OrderEventType.PAYMENT_SUCCEEDED);
    }

    @KafkaListener(topics = "payment_failed")
    public void handlePaymentFailed(CloudEvent event) {
        processEvent(event, OrderEventType.PAYMENT_FAILED);
    }

    @KafkaListener(topics = "shipment_succeeded")
    public void handleShipmentSucceeded(CloudEvent event) {
        processEvent(event, OrderEventType.SHIPMENT_SUCCEEDED);
    }

    @KafkaListener(topics = "shipment_failed")
    public void handleShipmentFailed(CloudEvent event) {
        processEvent(event, OrderEventType.SHIPMENT_FAILED);
    }

    private void processEvent(CloudEvent cloudEvent, OrderEventType eventType) {
        try {
            Order order = extractOrderFromEvent(cloudEvent);
            if (order == null) {
                log.warn("Could not extract order from CloudEvent with type: {}", cloudEvent.getType());
                return;
            }

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
        } catch (Exception e) {
            log.error("Error processing CloudEvent of type {}: {}", cloudEvent.getType(), e.getMessage());
        }
    }

    private Order extractOrderFromEvent(CloudEvent event) {
        if (event == null || event.getData() == null) {
            log.warn("Invalid CloudEvent or data null");
            return null;
        }
        try {
            byte[] data = event.getData().toBytes();
            OrderEvent orderEvent = objectMapper.readValue(data, OrderEvent.class);
            return orderEvent.order();
        } catch (Exception e) {
            log.error("Error deserializing Order from CloudEvent", e);
            return null;
        }
    }
}
