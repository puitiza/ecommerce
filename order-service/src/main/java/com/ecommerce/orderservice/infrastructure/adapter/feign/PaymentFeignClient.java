package com.ecommerce.orderservice.infrastructure.adapter.feign;

import com.ecommerce.orderservice.application.dto.RefundRequest;
import com.ecommerce.orderservice.application.dto.RefundResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "payment-service", path = "/payments")
public interface PaymentFeignClient {

    @PostMapping("/refund/{paymentId}")
    RefundResponse initiateRefund(@PathVariable("paymentId") String paymentId, @RequestBody RefundRequest refundRequest);

    @GetMapping("/findPaymentIdByOrderId")
    String findPaymentIdByOrderId(@RequestParam("orderId") String orderId);
}
