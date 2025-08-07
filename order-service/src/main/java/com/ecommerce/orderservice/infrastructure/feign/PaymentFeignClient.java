package com.ecommerce.orderservice.infrastructure.feign;

import com.ecommerce.orderservice.application.dto.PaymentAuthorizationResponse;
import com.ecommerce.orderservice.application.dto.RefundResponse;
import com.ecommerce.orderservice.application.request.PaymentAuthorizationRequest;
import com.ecommerce.orderservice.application.request.RefundRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "payment-service", path = "/payments")
public interface PaymentFeignClient {

    @PostMapping("/authorize")
    PaymentAuthorizationResponse authorizePayment(@RequestBody PaymentAuthorizationRequest request);

    @PostMapping("/refund/{paymentId}")
    RefundResponse initiateRefund(@PathVariable("paymentId") String paymentId, @RequestBody RefundRequest refundRequest);

    @GetMapping("/findPaymentIdByOrderId")
    String findPaymentIdByOrderId(@RequestParam("orderId") String orderId);
}
