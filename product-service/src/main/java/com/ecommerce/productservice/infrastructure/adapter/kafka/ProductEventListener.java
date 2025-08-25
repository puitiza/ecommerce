package com.ecommerce.productservice.infrastructure.adapter.kafka;

import com.ecommerce.productservice.application.service.ProductApplicationService;
import com.ecommerce.shared.domain.event.OrderEventPayload;
import com.ecommerce.shared.domain.event.OrderEventType;
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

/**
 * Kafka listener for consuming order-related events in the product service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductApplicationService productService;
    private final ObjectMapper objectMapper;

    // Subset of events that the listener consumes
    private static final OrderEventType[] CONSUMER_EVENTS = {
            OrderEventType.ORDER_CREATED,
            OrderEventType.RETRY_VALIDATION,
            OrderEventType.CANCEL
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
     * Processes incoming order events and delegates to the application service.
     */
    @KafkaListener(topics = "#{consumerTopics}", containerFactory = "kafkaListenerContainerFactory")
    public void handleEvent(@Payload CloudEvent cloudEvent,
                            @Header(value = KafkaHeaders.RECEIVED_TOPIC, required = false) String topic) {
        OrderEventType eventType = topicToEventTypeMap.get(topic);
        if (eventType == null) {
            log.warn("Received event on unknown topic: {}", topic);
            return;
        }
        extractOrderPayloadFromEvent(cloudEvent)
                .ifPresentOrElse(payload -> {
                            switch (eventType) {
                                case ORDER_CREATED, RETRY_VALIDATION -> productService.validateAndReserveStock(payload);
                                case CANCEL -> productService.restock(payload);
                                default -> log.warn("Unsupported event type: {}", eventType);
                            }
                        },
                        () -> log.warn("Could not extract order from CloudEvent with type: {}", cloudEvent.getType()));
    }

    /**
     * Extracts an {@link OrderEventPayload} from a CloudEvent.
     */
    private Optional<OrderEventPayload> extractOrderPayloadFromEvent(CloudEvent event) {
        if (event == null || event.getData() == null) {
            log.warn("Invalid CloudEvent or data is null");
            return Optional.empty();
        }
        try {
            PojoCloudEventData<OrderEventPayload> deserializedData = CloudEventUtils.mapData(
                    event, PojoCloudEventDataMapper.from(objectMapper, OrderEventPayload.class)
            );
            return Optional.ofNullable(deserializedData).map(PojoCloudEventData::getValue);
        } catch (Exception e) {
            log.error("Error deserializing OrderEventPayload from CloudEvent", e);
            return Optional.empty();
        }
    }
}