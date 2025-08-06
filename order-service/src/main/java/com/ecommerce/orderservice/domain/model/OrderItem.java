package com.ecommerce.orderservice.domain.model;

import java.math.BigDecimal;

public record OrderItem(
        Long id,
        Long productId,
        Integer quantity,
        BigDecimal unitPrice
) {}