package com.ecommerce.orderservice.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundResponse {
    private String refundId;
    private String status;
}
