package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.model.Order;
import com.ecommerce.userservice.model.OrderEventData;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public record UserController(KafkaTemplate<String, CloudEvent> kafkaTemplate) {

    @PostMapping("/sendOrder")
    public ResponseEntity<String> createOrder(@RequestBody Order order) {
        produceOrderEvent(order);
        return ResponseEntity.status(HttpStatus.CREATED).body("Order created successfully");
    }


    public void produceOrderEvent(Order order) {
        OrderEventData orderEventData = new OrderEventData(order);
        CloudEvent cloudEvent = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withType("OrderEventType")
                .withSource(URI.create("/order-service"))
                .withDataContentType("application/json")
                .withData(orderEventData) // Assuming Order is serialized to JSON
                .withSubject("com.ecommerce.event.report.order.created")
                .build();

        String kafkaRecordKey = UUID.randomUUID().toString();

        kafkaTemplate.send("order-topic", kafkaRecordKey, cloudEvent);
    }
}
