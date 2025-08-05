package com.ecommerce.orderservice.feign;

import com.ecommerce.orderservice.model.request.PaymentAuthorizationRequest;
import com.ecommerce.orderservice.model.request.RefundRequest;
import com.ecommerce.orderservice.model.response.PaymentAuthorizationResponse;
import com.ecommerce.orderservice.model.response.RefundResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "payment-service")
public interface PaymentFeignClient {

    @PostMapping("/payments/authorize")
    PaymentAuthorizationResponse authorizePayment(@RequestBody PaymentAuthorizationRequest authorizationRequest);

    @PostMapping("/payments/refund/{paymentId}")
    RefundResponse initiateRefund(@PathVariable String paymentId, @RequestBody RefundRequest refundRequest);

    @GetMapping("/payments/findPaymentIdByOrderId")
    String findPaymentIdByOrderId(@RequestParam String orderId);
}

