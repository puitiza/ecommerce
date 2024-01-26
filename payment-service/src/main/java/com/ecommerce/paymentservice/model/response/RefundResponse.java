package com.ecommerce.paymentservice.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RefundResponse {

    @Schema(description = "ID of the initiated refund")
    private String refundId;

    @Schema(description = "Status of the refund (e.g., PENDING, COMPLETED)")
    private String status;

    private String message;
}
