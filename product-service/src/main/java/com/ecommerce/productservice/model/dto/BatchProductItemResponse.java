package com.ecommerce.productservice.model.dto;

import java.math.BigDecimal;

public record BatchProductItemResponse(
        Long productId,
        String name,
        BigDecimal price,
        boolean isAvailable,
        Integer availableUnits,
        String error
) {
}
