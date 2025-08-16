package com.ecommerce.orderservice.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for the Batch products details response from the Product Service.
 * <p>
 * The {@code @JsonIgnoreProperties(ignoreUnknown = true)} annotation is used
 * to ensure that deserialization does not fail if the Product Service adds new fields
 * to its response. This promotes loose coupling and resilience.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Response object for product details")
public record BatchProductDetailsResponse(
        @Schema(description = "Product details if found")
        ProductResponse product,
        @Schema(description = "Error message if the product was not found")
        String error
) {
}
