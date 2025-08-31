package com.ecommerce.productservice.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Schema(description = "Represents an product response", requiredMode = Schema.RequiredMode.REQUIRED)
public record ProductResponse(

        @Schema(description = "Unique identifier for the product", example = "999")
        String id,

        @NotBlank(message = "name field not should be null or empty")
        @Schema(description = "Name of product", example = "xiaomi")
        String name,

        @Schema(description = "Description of the product", example = "A comfortable and stylish T-Shirt.")
        String description,

        @NotNull(message = "price field not should be null or empty")
        @Schema(description = "Price of the product", example = "29.99")
        BigDecimal price,

        @Schema(description = "Current inventory level of the product", example = "100")
        Integer inventory,

        @Schema(description = "URL of the product image", example = "https://example.com/product-image.png")
        String image,

        @Schema(description = "List of categories the product belongs to", example = "[\"Clothing\", \"T-Shirts\"]")
        Set<String> categories,

        @Schema(description = "Additional product metadata", example = "{\"color\": \"Black\", \"storage\": \"128GB\"}")
        Map<String, Object> additionalData,

        @Schema(description = "Date and time when the product was created", example = "2024-01-19T00:18:54.343")
        LocalDateTime createdAt,

        @Schema(description = "Date and time when the product was last updated", example = "2024-01-19T00:18:54.367")
        LocalDateTime updatedAt

) {
}
