package com.ecommerce.orderservice.publisher;

import com.ecommerce.orderservice.model.dto.OrderDto;
import com.ecommerce.orderservice.model.entity.OrderEntity;
import com.ecommerce.orderservice.model.event.OrderEventData;
import com.ecommerce.orderservice.model.event.OrderEventType;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.UUID;

@Slf4j
@Component
public class OrderEventPublisher {
    private final KafkaTemplate<String, CloudEvent> kafkaTemplate;
    private final ModelMapper modelMapper;

    public OrderEventPublisher(KafkaTemplate<String, CloudEvent> kafkaTemplate, ModelMapper modelMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.modelMapper = modelMapper;
    }

    public void publishOrderCreatedEvent(OrderEntity order) {
        publishEvent(order, OrderEventType.ORDER_CREATED);
    }

    public void publishOrderValidatedEvent(OrderEntity order) {
        publishEvent(order, OrderEventType.ORDER_VALIDATED_SUCCESS);
    }

    public void publishValidationFailedEvent(OrderEntity order) {
        publishEvent(order, OrderEventType.ORDER_VALIDATED_FAILED);
    }

    public void publishOrderCancelledEvent(OrderEntity order) {
        publishEvent(order, OrderEventType.ORDER_CANCELLED);
    }

    public void publishOrderUpdatedEvent(OrderEntity order) {
        publishEvent(order, OrderEventType.ORDER_UPDATED);
    }

    private void publishEvent(OrderEntity order, OrderEventType eventType) {
        CloudEvent cloudEvent = createCloudOrderEvent(order, eventType);
        String kafkaRecordKey = UUID.randomUUID().toString();
        kafkaTemplate.send(eventType.getTopic(), kafkaRecordKey, cloudEvent);
        log.info("Sent '{}' event to Kafka for order ID: {}", eventType.getEventType(), order.getId());
    }

    private CloudEvent createCloudOrderEvent(OrderEntity order, OrderEventType eventType) {
        var OrderDto = modelMapper.map(order, OrderDto.class);
        OrderEventData orderEventData = new OrderEventData(OrderDto);

        return CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withType(eventType.getEventType())
                .withSource(URI.create("/order-service"))
                .withDataContentType("application/json")
                .withData(orderEventData)
                .withSubject("com.ecommerce.event.report." + eventType.getSubject())
                .build();
    }
}
