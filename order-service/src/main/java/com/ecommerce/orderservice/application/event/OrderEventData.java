package com.ecommerce.orderservice.application.event;

import com.ecommerce.orderservice.application.dto.OrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.cloudevents.CloudEventData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record OrderEventData(OrderResponse order) implements CloudEventData {

    @Override
    public byte[] toBytes() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JavaTimeModule module = new JavaTimeModule();
            module.addSerializer(LocalDateTime.class,
                    new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")));
            mapper.registerModule(module);
            return mapper.writeValueAsBytes(order);
        } catch (Exception e) {
            // Handle serialization exception
            throw new RuntimeException("Error serializing Order to JSON", e);
        }
    }
}
