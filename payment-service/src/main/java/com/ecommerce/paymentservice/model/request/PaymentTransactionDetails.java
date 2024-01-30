package com.ecommerce.paymentservice.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class PaymentTransactionDetails {

    @Schema(description = "Unique identifier for the payment transaction", example = "98765432-1098-cdef-0123-456789012345")
    private String paymentId;

    @Schema(description = "ID of the order associated with the payment", example = "f4321b56-7890-abcd-ef01-234567890123")
    private String orderId;

    @Schema(description = "Payment method used by the customer", example = "CREDIT_CARD")
    private String paymentMethod;

    @Schema(description = "Total amount paid", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "Payment status", example = "SUCCESS")
    private String status;

    @Schema(description = "Date and time the payment was processed", example = "2023-12-11T10:50:00Z")
    private LocalDateTime createdAt;
}
