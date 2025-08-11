package com.ecommerce.orderservice.application.dto;

import java.math.BigDecimal;

public record RefundRequest(
        BigDecimal refundAmount
) {}