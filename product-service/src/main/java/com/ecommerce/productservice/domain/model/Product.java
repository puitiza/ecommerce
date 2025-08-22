package com.ecommerce.productservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public record Product(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer inventory,
        String image,
        Set<String> categories,
        Map<String, Object> additionalData,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}