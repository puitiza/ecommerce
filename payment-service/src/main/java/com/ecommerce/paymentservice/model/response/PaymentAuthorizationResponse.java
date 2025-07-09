package com.ecommerce.paymentservice.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentAuthorizationResponse {

    @Schema(description = "Indicates whether payment was authorized", example = "true")
    private boolean authorized;

    @Schema(description = "Optional message describing the payment outcome", example = "Payment completed successfully.")
    private String message;
}
