package com.ecommerce.orderservice.infrastructure.adapter.feign;

import com.ecommerce.orderservice.application.dto.PaymentAuthorizationResponse;
import com.ecommerce.orderservice.application.dto.RefundResponse;
import com.ecommerce.orderservice.application.port.out.PaymentServicePort;
import com.ecommerce.orderservice.application.dto.PaymentAuthorizationRequest;
import com.ecommerce.orderservice.application.dto.RefundRequest;
import com.ecommerce.orderservice.domain.exception.OrderValidationException;
import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFeignAdapter implements PaymentServicePort {
    private static final String SERVICE_NAME = "Payment";
    private final PaymentFeignClient paymentFeignClient;

    @Override
    @CircuitBreaker(name = "paymentAuthorizeCircuit", fallbackMethod = "authorizePaymentFallback")
    public PaymentAuthorizationResponse authorizePayment(PaymentAuthorizationRequest request) {
        return paymentFeignClient.authorizePayment(request);
    }

    @Override
    @CircuitBreaker(name = "paymentRefundCircuit", fallbackMethod = "initiateRefundFallback")
    public void initiateRefund(String paymentId, RefundRequest refundRequest) {
        paymentFeignClient.initiateRefund(paymentId, refundRequest);
    }

    @Override
    @CircuitBreaker(name = "paymentFindCircuit", fallbackMethod = "findPaymentIdFallback")
    public String findPaymentIdByOrderId(String orderId) {
        return paymentFeignClient.findPaymentIdByOrderId(orderId);
    }

    @SuppressWarnings("unused")
    private PaymentAuthorizationResponse authorizePaymentFallback(PaymentAuthorizationRequest request, Throwable t) {
        throw handleError(request.orderId(), t, "Failed to authorize payment", ExceptionError.ORDER_PAYMENT_AUTHORIZATION_FAILED);
    }

    @SuppressWarnings("unused")
    private RefundResponse initiateRefundFallback(String paymentId, RefundRequest refundRequest, Throwable t) {
        throw handleError(paymentId, t, "Failed to initiate refund", ExceptionError.ORDER_REFUND_FAILED);
    }

    @SuppressWarnings("unused")
    private String findPaymentIdFallback(String orderId, Throwable t) {
        throw handleError(orderId, t, "Failed to find payment ID", ExceptionError.ORDER_PAYMENT_LOOKUP_FAILED);
    }

    private RuntimeException handleError(String id, Throwable t, String msg, ExceptionError error) {
        log.error("{} for ID {}: {}", msg, id, t.getMessage(), t);
        String details = t.getMessage() != null ? t.getMessage() : String.format("%s for %sId %s", msg, SERVICE_NAME, id);
        if (t instanceof FeignException feignException) {
            return switch (feignException.status()) {
                case 404 -> new ResourceNotFoundException(SERVICE_NAME, id);
                case 429 -> new OrderValidationException(ExceptionError.GATEWAY_RATE_LIMIT,
                        String.format("Rate limit exceeded for %sId %s", SERVICE_NAME, id));
                default -> new OrderValidationException(error, details, SERVICE_NAME, id);
            };
        }
        return new OrderValidationException(ExceptionError.INTERNAL_SERVER_ERROR, details);
    }
}