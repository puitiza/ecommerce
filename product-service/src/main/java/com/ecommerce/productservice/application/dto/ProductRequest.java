package com.ecommerce.productservice.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@Schema(description = "Request object for creating a new product", requiredMode = Schema.RequiredMode.REQUIRED)
public record ProductRequest(

        @NotBlank(message = "name field not should be null or empty")
        @Schema(description = "Name of product", example = "xiaomi")
        String name,

        @Schema(description = "Description of the product", example = "A comfortable and stylish T-Shirt.")
        String description,

        @NotNull(message = "price field must not be null")
        @PositiveOrZero(message = "price must be zero or positive")
        @Schema(description = "Price of the product", example = "29.99")
        BigDecimal price,

        @NotNull(message = "inventory field must not be null")
        @PositiveOrZero(message = "inventory must be zero or positive")
        @Schema(description = "Current inventory level of the product", example = "100")
        Integer inventory,

        @Schema(description = "URL of the product image", example = "https://example.com/product-image.png")
        String image,

        @NotEmpty(message = "categories must not be empty")
        @Schema(description = "List of categories the product belongs to", example = "[\"Clothing\", \"T-Shirts\"]")
        Set<String> categories,

        @Schema(description = "Additional product metadata", example = "{\"color\": \"Black\", \"storage\": \"128GB\"}")
        Map<String, Object> additionalData
) {
}
