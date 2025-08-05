package com.ecommerce.orderservice.model.event;

import com.ecommerce.orderservice.model.dto.OrderDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEventData;

public record OrderEventData(OrderDto order) implements CloudEventData {

    @Override
    public byte[] toBytes() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsBytes(order);
        } catch (Exception e) {
            // Handle serialization exception
            throw new RuntimeException("Error serializing Order to JSON", e);
        }
    }
}
