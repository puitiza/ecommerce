package com.ecommerce.orderservice.application.dto;

public record ProductAvailabilityResponse(
        boolean isAvailable,
        Integer availableUnits
) {
}