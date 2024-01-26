package com.ecommerce.paymentservice.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PaymentMethodDetails {

    @Schema(description = "Unique identifier for card", example = "1234567890123456")
    private String cardNumber;

    @Schema(description = "Month of expiry your card", example = "12")
    private Integer expiryMonth;

    @Schema(description = "Year of expiry your card", example = "2025")
    private Integer expiryYear;

    @Schema(description = "Unique identifier for card", example = "123")
    private Integer cvv;

}
