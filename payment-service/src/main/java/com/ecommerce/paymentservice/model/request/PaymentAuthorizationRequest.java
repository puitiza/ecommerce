package com.ecommerce.paymentservice.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class PaymentAuthorizationRequest {

    @NotNull(message = "Order ID is required")
    @Schema(description = "ID of the order to authorize payment for")
    private String orderId;

    @NotNull(message = "Amount is required")
    @Schema(description = "Total amount to be authorized", example = "100.00")
    private BigDecimal amount;
}
