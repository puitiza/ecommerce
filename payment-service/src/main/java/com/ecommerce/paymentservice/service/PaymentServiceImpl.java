package com.ecommerce.paymentservice.service;

import com.ecommerce.paymentservice.configuration.exception.NoSuchElementFoundException;
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

@Slf4j
@Service
public record PaymentServiceImpl(PaymentRepository paymentRepository) implements PaymentService {
    @Override
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setOrderId(paymentRequest.getOrderId());
        paymentEntity.setPaymentMethod(paymentRequest.getPaymentMethod());
        //paymentEntity.setPaymentMethodDetails(paymentRequest.getPaymentMethodDetails());
        paymentEntity.setAmount(paymentRequest.getAmount());
        paymentEntity.setStatus(PaymentStatus.PENDING);
        var paymentSaved = paymentRepository.save(paymentEntity);

        // Publish payment-related events (if needed)
        // paymentEventPublisher.publishPaymentProcessedEvent(paymentEntity);

        // Return PaymentResponse
        return new PaymentResponse(
                paymentSaved.getId().toString(),
                paymentSaved.getOrderId(),
                paymentSaved.getStatus().name(),
                "Payment processing initiated successfully.",
                paymentSaved.getCreatedAt()
        );
    }

    @Override
    public PaymentAuthorizationResponse authorizePayment(PaymentAuthorizationRequest authorizationRequest) {
        var paymentFound = paymentRepository.findByOrderId(authorizationRequest.getOrderId())
                .orElseThrow(() -> new NoSuchElementFoundException("Payment not found for order: " + authorizationRequest.getOrderId(), "P01"));

        if (authorizationRequest.getAmount().compareTo(paymentFound.getAmount()) >= 0) {
            paymentFound.setStatus(PaymentStatus.SUCCESS);
            paymentRepository.save(paymentFound);

            // Publish authorization-related events (if needed)
            //paymentEventPublisher.publishPaymentAuthorizedEvent(paymentFound);

            return new PaymentAuthorizationResponse(true);
        } else {
            // Payment authorization failed
            paymentFound.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(paymentFound);

            // Publish authorization-related events (if needed)
            //paymentEventPublisher.publishPaymentAuthorizationFailedEvent(paymentFound);

            return new PaymentAuthorizationResponse(false);
        }
    }

    @Override
    public PaymentTransactionDetails getPaymentDetails(String paymentId) {
        // Implement logic to retrieve payment details from the database

        PaymentEntity paymentEntity = paymentRepository.findById(Long.parseLong(paymentId))
                .orElseThrow(() -> new NoSuchElementFoundException("Payment not found with ID: " + paymentId, "P01"));

        // Return PaymentTransactionDetails
        return new PaymentTransactionDetails(
                paymentEntity.getId().toString(),
                paymentEntity.getOrderId(),
                paymentEntity.getPaymentMethod(),
                paymentEntity.getAmount(),
                paymentEntity.getStatus().name(),
                "Payment details retrieved successfully.",
                paymentEntity.getCreatedAt()
        );
    }

    @Override
    public RefundResponse initiateRefund(String paymentId, RefundRequest refundRequest) {
        PaymentEntity paymentEntity = paymentRepository.findById(Long.parseLong(paymentId))
                .orElseThrow(() -> new NoSuchElementFoundException("Payment not found with ID: " + paymentId, "P01"));

        if (refundRequest.getRefundAmount().compareTo(paymentEntity.getAmount()) <= 0) {
            paymentEntity.setStatus(PaymentStatus.PENDING); // Set status to pending refund
            paymentRepository.save(paymentEntity);

            // Publish refund-related events (if needed)
            //paymentEventPublisher.publishRefundInitiatedEvent(paymentEntity);

            // Return RefundResponse
            return new RefundResponse(
                    paymentEntity.getId().toString(),
                    PaymentStatus.PENDING.name(),
                    "Refund initiated successfully."
            );
        } else {
            // Refund amount exceeds the original payment amount
            log.warn("Refund amount exceeds the original payment amount for payment ID: {}", paymentId);

            return new RefundResponse(
                    paymentEntity.getId().toString(),
                    PaymentStatus.FAILED.name(),
                    "Refund amount exceeds the original payment amount.");
        }
    }
}
