package com.ecommerce.orderservice.infrastructure.adapter.feign;

import com.ecommerce.orderservice.application.dto.PaymentAuthorizationResponse;
import com.ecommerce.orderservice.application.dto.RefundResponse;
import com.ecommerce.orderservice.application.port.out.PaymentServicePort;
import com.ecommerce.orderservice.application.request.PaymentAuthorizationRequest;
import com.ecommerce.orderservice.application.request.RefundRequest;
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
public class PaymentFeignClientAdapter implements PaymentServicePort {
    private static final String SERVICE_NAME = "Payment";
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

    @SuppressWarnings("unused")
    private PaymentAuthorizationResponse authorizePaymentFallback(PaymentAuthorizationRequest request, Throwable t) {
        throw handlePaymentError(request.orderId(), t, "Failed to authorize payment", ExceptionError.ORDER_PAYMENT_AUTHORIZATION_FAILED);
    }

    @SuppressWarnings("unused")
    private RefundResponse initiateRefundFallback(String paymentId, RefundRequest refundRequest, Throwable t) {
        throw handlePaymentError(paymentId, t, "Failed to initiate refund", ExceptionError.ORDER_REFUND_FAILED);
    }

    @SuppressWarnings("unused")
    private String findPaymentIdFallback(String orderId, Throwable t) {
        throw handlePaymentError(orderId, t, "Failed to find payment ID", ExceptionError.ORDER_PAYMENT_LOOKUP_FAILED);
    }

    private RuntimeException handlePaymentError(String id, Throwable t, String defaultMessage, ExceptionError defaultError) {
        log.error("{} for {}Id {}: {}", defaultMessage, SERVICE_NAME, id, t.getMessage());
        String details = t.getMessage() != null ? t.getMessage() : String.format("%s for %sId %s", defaultMessage, SERVICE_NAME, id);
        if (t instanceof FeignException feignException) {
            return switch (feignException.status()) {
                case 404 -> new ResourceNotFoundException(SERVICE_NAME, id);
                case 429 -> new OrderValidationException(ExceptionError.GATEWAY_RATE_LIMIT,
                        String.format("Rate limit exceeded for %sId %s", SERVICE_NAME, id));
                default -> new OrderValidationException(defaultError, details, SERVICE_NAME, id);
            };
        }
        return new OrderValidationException(ExceptionError.INTERNAL_SERVER_ERROR, details);
    }
}