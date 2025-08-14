package com.ecommerce.orderservice.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO for the payment authorization response from the Payment Service.
 * <p>
 * The {@code @JsonIgnoreProperties(ignoreUnknown = true)} annotation ensures
 * that deserialization is not affected if the Payment Service's API evolves
 * and includes new, unmapped fields.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentAuthorizationResponse(boolean authorized,
                                           String message) {
}
