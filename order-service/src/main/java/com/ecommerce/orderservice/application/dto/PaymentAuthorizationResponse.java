package com.ecommerce.orderservice.application.dto;

public record PaymentAuthorizationResponse(boolean authorized,
                                           String message) {
}
