package com.ecommerce.orderservice.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class RefundRequest {
    private Long orderId;
    private BigDecimal refundAmount;
}
