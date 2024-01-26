package com.ecommerce.paymentservice.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentAuthorizationResponse {

    @Schema(description = "Indicates whether payment was authorized")
    private boolean authorized;
}
