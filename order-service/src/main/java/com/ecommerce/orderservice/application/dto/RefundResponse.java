package com.ecommerce.orderservice.application.dto;

public record RefundResponse(
        String refundId,
        String status
) {}