package com.ecommerce.orderservice.application.request;

import java.math.BigDecimal;

public record RefundRequest(
        BigDecimal refundAmount
) {}