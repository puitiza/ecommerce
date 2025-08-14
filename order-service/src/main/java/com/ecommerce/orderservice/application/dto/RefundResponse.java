package com.ecommerce.orderservice.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO for the refund response from the Payment Service.
 * <p>
 * The {@code @JsonIgnoreProperties(ignoreUnknown = true)} annotation is used
 * to make the service resilient to changes in the Payment Service API,
 * allowing it to ignore any new fields that may be added to the response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RefundResponse(
        String refundId,
        String status
) {}