package com.ecommerce.paymentservice.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class PaymentRequest {

    @NotNull(message = "Order ID is required")
    @Schema(description = "ID of the order for which payment is being processed", example = "f4321b56-7890-abcd-ef01-234567890123")
    private String orderId;

    @NotNull(message = "Payment method is required")
    @Schema(description = "Payment method used by the customer (e.g., credit card, PayPal)", example = "CREDIT_CARD")
    private String paymentMethod;

    @JsonProperty("paymentMethodDetails")
    private PaymentMethodDetails paymentMethodDetails;

    @NotNull(message = "Amount is required")
    @Schema(description = "Total amount to be paid", example = "100.00")
    private BigDecimal amount;
}
