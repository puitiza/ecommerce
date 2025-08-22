package com.ecommerce.productservice.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Paginated response for lists of products")
public record ProductPageResponse(
        @Schema(description = "List of orders in the current page")
        List<ProductResponse> content,
        @Schema(description = "Current page number", example = "0")
        int pageNumber,
        @Schema(description = "Number of items per page", example = "10")
        int pageSize,
        @Schema(description = "Total number of orders", example = "100")
        long totalElements,
        @Schema(description = "Total number of pages", example = "10")
        int totalPages
) {
    public ProductPageResponse(Page<ProductResponse> page) {
        this(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}