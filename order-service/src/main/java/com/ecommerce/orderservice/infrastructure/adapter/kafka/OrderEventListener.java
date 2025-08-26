package com.ecommerce.orderservice.infrastructure.adapter.kafka;

import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.shared.domain.event.OrderEventPayload;
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
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderEventProcessor eventProcessor;
    private final ObjectMapper objectMapper;

    // Subset of events that the listener consumes
    private static final OrderEventType[] CONSUMER_EVENTS = {
            OrderEventType.VALIDATION_SUCCEEDED,
            OrderEventType.VALIDATION_FAILED,
            OrderEventType.PAYMENT_SUCCEEDED,
            OrderEventType.PAYMENT_FAILED,
            OrderEventType.SHIPMENT_SUCCEEDED,
            OrderEventType.SHIPMENT_FAILED
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

    /**
     * Kafka listener for processing order-related events.
     * It deserializes the incoming CloudEvent and delegates its processing
     * to the OrderEventProcessor to handle the state machine logic in a transactional context.
     *
     * @param cloudEvent The incoming CloudEvent payload.
     * @param topic      The Kafka topic the event was received from.
     */
    @KafkaListener(topics = "#{consumerTopics}", containerFactory = "kafkaListenerContainerFactory")
    public void handleEvent(@Payload CloudEvent cloudEvent,
                            @Header(value = KafkaHeaders.RECEIVED_TOPIC, required = false) String topic) {
        OrderEventType eventType = topicToEventTypeMap.get(topic);
        if (eventType == null) {
            log.warn("Received event on unknown topic: {}", topic);
            return;
        }
        extractOrderPayloadFromEvent(cloudEvent).ifPresentOrElse(
                payload -> eventProcessor.processEvent(payload, eventType),
                () -> log.warn("Could not extract order payload from CloudEvent with type: {}", cloudEvent.getType())
        );
    }

    private Optional<OrderEventPayload> extractOrderPayloadFromEvent(CloudEvent event) {
        if (event == null || event.getData() == null) {
            log.warn("Invalid CloudEvent or data is null");
            return Optional.empty();
        }
        try {
            PojoCloudEventData<OrderEventPayload> deserializedData = CloudEventUtils.mapData(
                    event, PojoCloudEventDataMapper.from(objectMapper, OrderEventPayload.class));
            return Optional.ofNullable(deserializedData).map(PojoCloudEventData::getValue);
        } catch (Exception e) {
            log.error("Error deserializing OrderEventPayload from CloudEvent", e);
            return Optional.empty();
        }
    }
}