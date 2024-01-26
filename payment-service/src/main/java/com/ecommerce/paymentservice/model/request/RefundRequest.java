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
public class RefundRequest {

    @NotNull(message = "Order ID is required")
    @Schema(description = "ID of the order to refund")
    private String orderId;

    @NotNull(message = "Refund amount is required")
    @Schema(description = "Amount to be refunded", example = "50.00")
    private BigDecimal refundAmount;
}
