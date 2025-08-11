package com.ecommerce.orderservice.application.port.out;

import com.ecommerce.orderservice.application.dto.PaymentAuthorizationResponse;
import com.ecommerce.orderservice.application.dto.PaymentAuthorizationRequest;
import com.ecommerce.orderservice.application.dto.RefundRequest;

public interface PaymentServicePort {
    PaymentAuthorizationResponse authorizePayment(PaymentAuthorizationRequest request);

    void initiateRefund(String paymentId, RefundRequest refundRequest);

    String findPaymentIdByOrderId(String orderId);
}