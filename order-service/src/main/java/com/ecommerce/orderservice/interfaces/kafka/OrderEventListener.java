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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderRepositoryPort repositoryPort;
    private final StateMachineFactory<OrderStatus, OrderEventType> stateMachineFactory;
    private final ObjectMapper objectMapper;

    // Subset of events that the listener consumes
    private static final OrderEventType[] CONSUMER_EVENTS = {
            OrderEventType.VALIDATION_SUCCEEDED,
            OrderEventType.VALIDATION_FAILED,
            OrderEventType.PAYMENT_SUCCEEDED,
            OrderEventType.PAYMENT_FAILED,
            OrderEventType.SHIPMENT_SUCCEEDED,
            OrderEventType.SHIPMENT_FAILED,
            //next delete
            OrderEventType.ORDER_CREATED
    };

    // Map topics to OrderEventType for quick lookup
    private final Map<String, OrderEventType> topicToEventTypeMap = Arrays.stream(CONSUMER_EVENTS)
            .collect(Collectors.toMap(OrderEventType::getTopic, Function.identity()));

    @Configuration
    static class KafkaListenerConfig {
        @Bean
        public String[] consumerTopics() {
            return Arrays.stream(CONSUMER_EVENTS)
                    .map(OrderEventType::getTopic)
                    .toArray(String[]::new);
        }
    }

    @KafkaListener(topics = "#{consumerTopics}")
    public void handleEvent(@Payload CloudEvent cloudEvent,
                            @Header(value = KafkaHeaders.RECEIVED_TOPIC, required = false) String topic) {
        OrderEventType eventType = topicToEventTypeMap.get(topic);
        if (eventType == null) {
            log.warn("Received event on unknown topic: {}", topic);
            return;
        }
        handleTest(cloudEvent);
        //processEvent(cloudEvent, eventType);
    }

    //@KafkaListener(topics = "order_created")
    public void handleTest(CloudEvent cloudEvent) {
        extractOrderFromEvent(cloudEvent)
                .ifPresentOrElse(order -> {
                    log.info("Received cloudEvent. Id: {}; Data: {}", cloudEvent.getId(), order.toString());
                }, () -> log.warn("Could not extract order" + cloudEvent.getType()));
    }

    /**
     * Centralized method to process any incoming CloudEvent and advance the state machine.
     *
     * @param cloudEvent The incoming CloudEvent.
     * @param eventType  The type of the event, which corresponds to the state machine transition.
     */
    //@Transactional
    private void processEvent(CloudEvent cloudEvent, OrderEventType eventType) {
        extractOrderFromEvent(cloudEvent).ifPresentOrElse(order -> {
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