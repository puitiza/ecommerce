package com.ecommerce.orderservice.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * DTO for the response from the Product Service.
 * <p>
 * The {@code @JsonIgnoreProperties(ignoreUnknown = true)} annotation is used
 * to ensure that deserialization does not fail if the Product Service adds new fields
 * to its response in the future. This promotes loose coupling and resilience
 * between microservices.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer inventory
) {
}