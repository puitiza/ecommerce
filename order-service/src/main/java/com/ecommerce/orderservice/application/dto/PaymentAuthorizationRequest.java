package com.ecommerce.orderservice.application.dto;

import java.math.BigDecimal;

public record PaymentAuthorizationRequest(
        String orderId,
        BigDecimal amount
) {}