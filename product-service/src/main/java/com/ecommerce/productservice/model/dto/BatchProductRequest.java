package com.ecommerce.productservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request object for creating a batch of Product")
public record BatchProductRequest(

        @Valid
        @Schema(description = "List of items to order", example = "[{productId: 1, quantity: 2}, {productId: 4, quantity: 1}]")
        @NotNull(message = "The list of items cannot be null")
        @NotEmpty(message = "The list of items cannot be empty")
        List<BatchProductItemRequest> items
) {
}