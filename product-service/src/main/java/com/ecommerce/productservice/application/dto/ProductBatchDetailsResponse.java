package com.ecommerce.productservice.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents the detailed response for a product in a batch query")
public record ProductBatchDetailsResponse(
        @Schema(description = "Product details if found")
        ProductResponse product,
        @Schema(description = "Error message if the product was not found")
        String error
) {
}
