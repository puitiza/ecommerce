package com.ecommerce.userservice.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEventData;


public record OrderEventData(Order order) implements CloudEventData {

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
