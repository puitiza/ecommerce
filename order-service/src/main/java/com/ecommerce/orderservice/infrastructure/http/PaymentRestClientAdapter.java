package com.ecommerce.orderservice.infrastructure.http;

import com.ecommerce.orderservice.application.dto.PaymentAuthorizationResponse;
import com.ecommerce.orderservice.application.dto.RefundResponse;
import com.ecommerce.orderservice.application.port.out.PaymentServicePort;
import com.ecommerce.orderservice.application.request.PaymentAuthorizationRequest;
import com.ecommerce.orderservice.application.request.RefundRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentRestClientAdapter implements PaymentServicePort {
    private final RestClient restClient;

    public PaymentRestClientAdapter(RestClient paymentRestClient) {
        this.restClient = paymentRestClient;
    }

    @Override
    public PaymentAuthorizationResponse authorizePayment(PaymentAuthorizationRequest request) {
        return restClient.post()
                .uri("/payments/authorize")
                .body(request)
                .retrieve()
                .body(PaymentAuthorizationResponse.class);
    }

    @Override
    public RefundResponse initiateRefund(String paymentId, RefundRequest refundRequest) {
        return restClient.post()
                .uri("/payments/refund/{paymentId}", paymentId)
                .body(refundRequest)
                .retrieve()
                .body(RefundResponse.class);
    }

    @Override
    public String findPaymentIdByOrderId(String orderId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/payments/findPaymentIdByOrderId")
                        .queryParam("orderId", orderId)
                        .build())
                .retrieve()
                .body(String.class);
    }
}