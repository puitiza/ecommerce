package com.ecommerce.paymentservice.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaymentResponse {

    @Schema(description = "Unique identifier for the payment transaction", example = "98765432-1098-cdef-0123-456789012345")
    private String paymentId;

    @Schema(description = "ID of the order associated with the payment", example = "f4321b56-7890-abcd-ef01-234567890123")
    private String orderId;

    @Schema(description = "Payment status (e.g., SUCCESS, FAILED)", example = "SUCCESS")
    private String status;

    @Schema(description = "Optional message describing the payment outcome", example = "Payment completed successfully.")
    private String message;

}
