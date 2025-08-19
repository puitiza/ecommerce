package com.ecommerce.orderservice.application.dto;

import java.util.List;

/**
 * DTO for the Batch product response from the Product Service.
 * Deserialization resilience is handled by ObjectMapper configuration.
 */
public record BatchProductResponse(List<BatchProductItemResponse> products) {
}

