package com.ecommerce.orderservice.application.request;

import java.math.BigDecimal;

public record PaymentAuthorizationRequest(
        String orderId,
        BigDecimal amount
) {}