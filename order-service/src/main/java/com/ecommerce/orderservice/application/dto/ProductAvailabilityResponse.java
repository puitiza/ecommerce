package com.ecommerce.orderservice.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO for the product availability response from the Product Service.
 * <p>
 * The {@code @JsonIgnoreProperties(ignoreUnknown = true)} annotation is used
 * to ensure that deserialization does not fail if the Product Service adds new fields
 * to its response. This promotes loose coupling and resilience.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductAvailabilityResponse(
        boolean isAvailable,
        Integer availableUnits
) {
}