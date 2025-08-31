package com.ecommerce.productservice.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request object for fetching product details in batch")
public record ProductBatchDetailsRequest(

        @Schema(description = "List of product IDs", example = "[1, 2, 3]")
        @NotNull(message = "The list of product IDs cannot be null")
        @NotEmpty(message = "The list of product IDs cannot be empty")
        List<Long> productIds
) {
}
