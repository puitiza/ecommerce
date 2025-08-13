package com.ecommerce.orderservice.interfaces.kafka;

import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import com.ecommerce.orderservice.domain.port.OrderRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.CloudEventUtils;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.jackson.PojoCloudEventDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
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
    private final ObjectMapper objectMapper; // Spring will inject the Bean configured

    @KafkaListener(topics = "${kafka.topics.validation-succeeded}")
    public void handleValidationSucceeded(@Payload CloudEvent event) {
        processEvent(event, OrderEventType.VALIDATION_SUCCEEDED);
    }

    @KafkaListener(topics = "validation_failed")
    public void handleValidationFailed(@Payload CloudEvent event) {
        processEvent(event, OrderEventType.VALIDATION_FAILED);
    }

    @KafkaListener(topics = "payment_succeeded")
    public void handlePaymentSucceeded(@Payload CloudEvent event) {
        processEvent(event, OrderEventType.PAYMENT_SUCCEEDED);
    }

    @KafkaListener(topics = "payment_failed")
    public void handlePaymentFailed(@Payload CloudEvent event) {
        processEvent(event, OrderEventType.PAYMENT_FAILED);
    }

    @KafkaListener(topics = "shipment_succeeded")
    public void handleShipmentSucceeded(@Payload CloudEvent event) {
        processEvent(event, OrderEventType.SHIPMENT_SUCCEEDED);
    }

    @KafkaListener(topics = "shipment_failed")
    public void handleShipmentFailed(@Payload CloudEvent event) {
        processEvent(event, OrderEventType.SHIPMENT_FAILED);
    }

    /**
     * Centralized method to process any incoming CloudEvent and advance the state machine.
     *
     * @param cloudEvent The incoming CloudEvent.
     * @param eventType  The type of the event, which corresponds to the state machine transition.
     */
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
            log.warn("Invalid CloudEvent or data is null");
            return null;
        }
        try {
            PojoCloudEventData<Order> deserializedData = CloudEventUtils.mapData(event, PojoCloudEventDataMapper.from(objectMapper, Order.class));
            if (deserializedData != null) {
                return deserializedData.getValue();
            } else {
                log.warn("Could not deserialize CloudEvent data for event ID: {}", event.getId());
                return null;
            }
        } catch (Exception e) {
            log.error("Error deserializing Order from CloudEvent", e);
            return null;
        }
    }
}