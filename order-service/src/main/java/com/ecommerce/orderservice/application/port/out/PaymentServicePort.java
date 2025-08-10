package com.ecommerce.orderservice.application.port.out;

import com.ecommerce.orderservice.application.dto.PaymentAuthorizationResponse;
import com.ecommerce.orderservice.application.dto.RefundResponse;
import com.ecommerce.orderservice.application.request.PaymentAuthorizationRequest;
import com.ecommerce.orderservice.application.request.RefundRequest;

public interface PaymentServicePort {
    PaymentAuthorizationResponse authorizePayment(PaymentAuthorizationRequest request);

    RefundResponse initiateRefund(String paymentId, RefundRequest refundRequest);

    String findPaymentIdByOrderId(String orderId);
}