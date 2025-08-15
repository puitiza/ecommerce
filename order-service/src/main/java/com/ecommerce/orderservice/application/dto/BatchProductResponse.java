package com.ecommerce.orderservice.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * DTO for the Batch product response from the Product Service.
 * <p>
 * The {@code @JsonIgnoreProperties(ignoreUnknown = true)} annotation is used
 * to ensure that deserialization does not fail if the Product Service adds new fields
 * to its response. This promotes loose coupling and resilience.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BatchProductResponse(List<BatchProductItemResponse> products) {
}

