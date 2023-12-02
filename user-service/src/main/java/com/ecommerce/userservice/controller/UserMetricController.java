package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.model.Order;
import com.ecommerce.userservice.model.OrderEventData;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/user-metrics")
public record UserMetricController(KafkaTemplate<String, CloudEvent> kafkaTemplate,
                                   Tracer tracer, ObservationRegistry observationRegistry) {

    @PostMapping("/sendOrder")
    public ResponseEntity<String> createOrder(@RequestBody Order order) throws Exception {

        produceOrderEvent_2(order);
        return ResponseEntity.status(HttpStatus.CREATED).body("Order created successfully");
    }

    void produceOrderEvent_2(Order order) throws Exception {
        Observation.createNotStarted("kafka-producer", this.observationRegistry).observeChecked(() -> {
            log.info("<ACCEPTANCE_TEST> <TRACE:{}> Hello from producer", this.tracer.currentSpan().context().traceId());
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
            CompletableFuture<SendResult<String, CloudEvent>> future = kafkaTemplate.send("order-topic", kafkaRecordKey, cloudEvent);
            return future.handle((result, throwable) -> {
                log.info("Result <{}>, throwable <{}>", result, throwable);
                return CompletableFuture.completedFuture(result);
            });
        }).get();
    }

}
