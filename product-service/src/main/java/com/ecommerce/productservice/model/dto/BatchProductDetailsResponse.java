package com.ecommerce.productservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Represents the detailed response for a product in a batch query")
public record BatchProductDetailsResponse(
        @Schema(description = "Product details if found")
        ProductDto product,
        @Schema(description = "Error message if the product was not found")
        String error
) {
}
