package com.ecommerce.productservice.application.dto;

import java.math.BigDecimal;

public record ProductBatchItemResponse(
        Long productId,
        String name,
        BigDecimal price,
        boolean isAvailable,
        Integer availableUnits,
        String error
) {
}
