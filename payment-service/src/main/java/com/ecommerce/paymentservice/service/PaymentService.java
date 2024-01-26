package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.model.request.PaymentAuthorizationRequest;
import com.ecommerce.paymentservice.model.request.PaymentRequest;
import com.ecommerce.paymentservice.model.request.PaymentTransactionDetails;
import com.ecommerce.paymentservice.model.request.RefundRequest;
import com.ecommerce.paymentservice.model.response.PaymentAuthorizationResponse;
import com.ecommerce.paymentservice.model.response.PaymentResponse;
import com.ecommerce.paymentservice.model.response.RefundResponse;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest paymentRequest);

    PaymentAuthorizationResponse authorizePayment(PaymentAuthorizationRequest authorizationRequest);

    PaymentTransactionDetails getPaymentDetails(String paymentId);

    RefundResponse initiateRefund(String paymentId, RefundRequest refundRequest);
}
