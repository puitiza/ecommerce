package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.configuration.exception.handler.ResourceNotFoundException;
import com.ecommerce.paymentservice.model.entity.PaymentEntity;
import com.ecommerce.paymentservice.model.entity.PaymentStatus;
import com.ecommerce.paymentservice.model.request.PaymentAuthorizationRequest;
import com.ecommerce.paymentservice.model.request.PaymentRequest;
import com.ecommerce.paymentservice.model.request.PaymentTransactionDetails;
import com.ecommerce.paymentservice.model.request.RefundRequest;
import com.ecommerce.paymentservice.model.response.PaymentAuthorizationResponse;
import com.ecommerce.paymentservice.model.response.PaymentResponse;
import com.ecommerce.paymentservice.model.response.RefundResponse;
import com.ecommerce.paymentservice.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.UUID;

@Slf4j
@Service
public record PaymentServiceImpl(PaymentRepository paymentRepository) implements PaymentService {
    @Override
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {

        try {
            PaymentEntity payment = new PaymentEntity();
            payment.setOrderId(paymentRequest.getOrderId());
            payment.setPaymentMethod(paymentRequest.getPaymentMethod());
            payment.setAmount(paymentRequest.getAmount());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setCreatedAt(ZonedDateTime.now().toLocalDateTime());
            var paymentSaved = paymentRepository.save(payment);

            /*
            // Initiate payment with payment gateway
            PaymentGatewayResponse gatewayResponse = paymentGatewayClient.processPayment(
                    paymentRequest.getPaymentMethod(), paymentRequest.getPaymentMethodDetails(),
                    paymentRequest.getAmount()
            );
             // Handle gateway response
            if (gatewayResponse.isSuccess()) {
                PaymentEntity paymentEntity = createPaymentEntity(paymentRequest, gatewayResponse);
                paymentRepository.save(paymentEntity);
                return createPaymentResponse(paymentEntity.getPaymentId(), "SUCCESS", "Payment processed successfully");
            } else {
                return createPaymentResponse(null, "FAILED", gatewayResponse.getErrorMessage());
            }
            */

            return createPaymentResponse(paymentSaved, PaymentStatus.SUCCESS.name(), "Payment processed successfully");
        } catch (Exception ex) {
            log.error("Payment processing failed:", ex);
            return createPaymentResponse(null, PaymentStatus.FAILED.name(), "An unexpected error occurred");
        }
    }

    private PaymentResponse createPaymentResponse(PaymentEntity payment, String status, String message) {
        return new PaymentResponse(
                payment != null ? payment.getId().toString() : null,
                payment != null ? payment.getOrderId() : null,
                status, message);
    }

    @Override
    public PaymentAuthorizationResponse authorizePayment(PaymentAuthorizationRequest authorizationRequest) {
        final long paymentCount = paymentRepository.countByOrderId(authorizationRequest.getOrderId());

        if (paymentCount > 0) {
            return new PaymentAuthorizationResponse(false, "Payment already exists for this order.");
        } else {
            try {
                // Create a new payment record with a PENDING status
                PaymentEntity payment = new PaymentEntity();
                payment.setOrderId(authorizationRequest.getOrderId());
                payment.setAmount(authorizationRequest.getAmount());
                payment.setStatus(PaymentStatus.PENDING);
                payment.setCreatedAt(ZonedDateTime.now().toLocalDateTime());
                paymentRepository.save(payment);

                return new PaymentAuthorizationResponse(true, "Payment authorized successfully.");
            } catch (Exception ex) {
                log.error("Payment authorization failed:", ex);
                return new PaymentAuthorizationResponse(false, "An unexpected error occurred during payment authorization.");
            }
        }
    }

    @Override
    public PaymentTransactionDetails getPaymentDetails(UUID paymentId) {
        PaymentEntity paymentEntity = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId, "P01"));

        return new PaymentTransactionDetails(
                paymentEntity.getId().toString(),
                paymentEntity.getOrderId(),
                paymentEntity.getPaymentMethod(),
                paymentEntity.getAmount(),
                paymentEntity.getStatus().name(),
                paymentEntity.getCreatedAt()
        );
    }

    @Override
    public RefundResponse initiateRefund(UUID paymentId, RefundRequest refundRequest) {
        PaymentEntity paymentEntity = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId, "P01"));

        if (refundRequest.getRefundAmount().compareTo(paymentEntity.getAmount()) <= 0) {
            paymentEntity.setUpdatedAt(ZonedDateTime.now().toLocalDateTime());
            paymentEntity.setStatus(PaymentStatus.PENDING); // Set status to pending refund
            paymentRepository.save(paymentEntity);
            return new RefundResponse(
                    paymentEntity.getId().toString(),
                    PaymentStatus.PENDING.name(),
                    "Refund initiated successfully."
            );
        } else {
            log.warn("Refund amount exceeds the original payment amount for payment ID: {}", paymentId);
            return new RefundResponse(
                    paymentEntity.getId().toString(),
                    PaymentStatus.FAILED.name(),
                    "Refund amount exceeds the original payment amount.");
        }
    }
}
