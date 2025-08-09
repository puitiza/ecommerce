package com.ecommerce.orderservice.infrastructure.http;

import com.ecommerce.orderservice.application.dto.PaymentAuthorizationResponse;
import com.ecommerce.orderservice.application.dto.RefundResponse;
import com.ecommerce.orderservice.application.port.out.PaymentServicePort;
import com.ecommerce.orderservice.application.request.PaymentAuthorizationRequest;
import com.ecommerce.orderservice.application.request.RefundRequest;
import com.ecommerce.orderservice.domain.exception.OrderValidationException;
import com.ecommerce.orderservice.infrastructure.feign.PaymentFeignClient;
import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFeignClientAdapter implements PaymentServicePort {
    private final PaymentFeignClient paymentFeignClient;

    @Override
    @CircuitBreaker(name = "paymentAuthorizeCircuit", fallbackMethod = "authorizePaymentFallback")
    public PaymentAuthorizationResponse authorizePayment(PaymentAuthorizationRequest request) {
        return paymentFeignClient.authorizePayment(request);
    }

    @Override
    @CircuitBreaker(name = "paymentRefundCircuit", fallbackMethod = "initiateRefundFallback")
    public RefundResponse initiateRefund(String paymentId, RefundRequest refundRequest) {
        return paymentFeignClient.initiateRefund(paymentId, refundRequest);
    }

    @Override
    @CircuitBreaker(name = "paymentFindCircuit", fallbackMethod = "findPaymentIdFallback")
    public String findPaymentIdByOrderId(String orderId) {
        return paymentFeignClient.findPaymentIdByOrderId(orderId);
    }

    private PaymentAuthorizationResponse authorizePaymentFallback(PaymentAuthorizationRequest request, Throwable t) {
        throw handlePaymentError(ExceptionError.ORDER_PAYMENT_AUTHORIZATION_FAILED, request.orderId(), t);
    }

    private RefundResponse initiateRefundFallback(String paymentId, RefundRequest refundRequest, Throwable t) {
        throw handlePaymentError(ExceptionError.ORDER_REFUND_FAILED, paymentId, t);
    }

    private String findPaymentIdFallback(String orderId, Throwable t) {
        throw handlePaymentError(ExceptionError.ORDER_PAYMENT_LOOKUP_FAILED, orderId, t);
    }

    private RuntimeException handlePaymentError(ExceptionError defaultError, String id, Throwable t) {
        log.error("Payment operation failed for id {}: {}", id, t.getMessage());
        if (t instanceof FeignException feignException) {
            int status = feignException.status();
            return switch (status) {
                case 404 -> new ResourceNotFoundException("Payment", id);
                case 429 -> new OrderValidationException(ExceptionError.GATEWAY_RATE_LIMIT, id, t.getMessage());
                case 503 -> new OrderValidationException(defaultError, id, t.getMessage());
                default -> status >= 400 && status < 500
                        ? new OrderValidationException(ExceptionError.VALIDATION_ERROR, id, t.getMessage())
                        : new OrderValidationException(defaultError, id, t.getMessage());
            };
        } else if (t instanceof CallNotPermittedException) {
            return new OrderValidationException(defaultError, id, "Payment service is currently unavailable");
        }
        return new OrderValidationException(defaultError, id, t.getMessage());
    }
}