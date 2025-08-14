package com.ecommerce.orderservice.application.port.out;

import com.ecommerce.orderservice.application.dto.RefundRequest;

public interface PaymentServicePort {

    void initiateRefund(String paymentId, RefundRequest refundRequest);

    String findPaymentIdByOrderId(String orderId);
}