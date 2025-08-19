package com.ecommerce.orderservice.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for product details response from the Product Service.
 * Deserialization resilience is handled by ObjectMapper configuration.
 */
@Schema(description = "Response object for product details")
public record BatchProductDetailsResponse(
        @Schema(description = "Product details if found")
        ProductResponse product,
        @Schema(description = "Error message if the product was not found")
        String error
) {
}
