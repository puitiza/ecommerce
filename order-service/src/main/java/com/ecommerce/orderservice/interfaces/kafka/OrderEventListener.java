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
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderRepositoryPort repositoryPort;
    private final StateMachineFactory<OrderStatus, OrderEventType> stateMachineFactory;
    private final ObjectMapper objectMapper; // Spring will inject the Bean configured

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

    /**
     * Centralized method to process any incoming CloudEvent and advance the state machine.
     *
     * @param cloudEvent The incoming CloudEvent.
     * @param eventType  The type of the event, which corresponds to the state machine transition.
     */
    //@Transactional
    private void processEvent(CloudEvent cloudEvent, OrderEventType eventType) {
        extractOrderFromEvent(cloudEvent)
                .ifPresentOrElse(order -> {
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
        }, () -> log.warn("Could not extract order from CloudEvent with type: {}", cloudEvent.getType()));
    }

    private Optional<Order> extractOrderFromEvent(CloudEvent event) {
        if (event == null || event.getData() == null) {
            log.warn("Invalid CloudEvent or data is null");
            return Optional.empty();
        }
        try {
            PojoCloudEventData<Order> deserializedData = CloudEventUtils.mapData(event, PojoCloudEventDataMapper.from(objectMapper, Order.class));
            return Optional.ofNullable(deserializedData).map(PojoCloudEventData::getValue);
        } catch (Exception e) {
            log.error("Error deserializing Order from CloudEvent", e);
            return Optional.empty();
        }
    }
}