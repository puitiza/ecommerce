package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.controller.openApi.PaymentOpenApi;
import com.ecommerce.paymentservice.model.request.PaymentAuthorizationRequest;
import com.ecommerce.paymentservice.model.request.PaymentRequest;
import com.ecommerce.paymentservice.model.request.PaymentTransactionDetails;
import com.ecommerce.paymentservice.model.request.RefundRequest;
import com.ecommerce.paymentservice.model.response.PaymentAuthorizationResponse;
import com.ecommerce.paymentservice.model.response.PaymentResponse;
import com.ecommerce.paymentservice.model.response.RefundResponse;
import com.ecommerce.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/payments")
public record PaymentController(PaymentService paymentService) implements PaymentOpenApi {

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse processPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        log.info("PROCESSING PAYMENT: {}", paymentRequest.toString());
        return paymentService.processPayment(paymentRequest);
    }

    @GetMapping(value = "/{paymentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public PaymentTransactionDetails getPaymentDetails(@PathVariable UUID paymentId) {
        log.info("GETTING PAYMENT DETAILS FOR PAYMENT ID: {}", paymentId);
        return paymentService.getPaymentDetails(paymentId);
    }

    @PostMapping(value = "/authorize", produces = MediaType.APPLICATION_JSON_VALUE)
    public PaymentAuthorizationResponse authorizePayment(@Valid @RequestBody PaymentAuthorizationRequest authorizationRequest) {
        log.info("AUTHORIZING PAYMENT: {}", authorizationRequest.toString());
        return paymentService.authorizePayment(authorizationRequest);
    }

    @PostMapping(value = "/refund/{paymentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public RefundResponse initiateRefund(@PathVariable UUID paymentId, @Valid @RequestBody RefundRequest refundRequest) {
        log.info("INITIATING REFUND FOR PAYMENT ID: {} WITH REQUEST: {}", paymentId, refundRequest);
        return paymentService.initiateRefund(paymentId, refundRequest);
    }
}

