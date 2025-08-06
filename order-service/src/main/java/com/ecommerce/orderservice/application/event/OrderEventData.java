package com.ecommerce.orderservice.application.event;

import com.ecommerce.orderservice.application.dto.OrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEventData;

public record OrderEventData(OrderResponse order) implements CloudEventData {

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
