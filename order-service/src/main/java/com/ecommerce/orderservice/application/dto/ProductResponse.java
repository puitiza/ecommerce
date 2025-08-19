package com.ecommerce.orderservice.application.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal price,
        Integer inventory
) {
}