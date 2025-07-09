package com.ecommerce.paymentservice.repository;

import com.ecommerce.paymentservice.model.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {

    long countByOrderId(String orderId);
}
