package com.ecommerce.orderservice.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentAuthorizationResponse {
    private boolean authorized;
    private String message;
}
