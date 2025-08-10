package com.ecommerce.orderservice.infrastructure.adapter.persistence.repository;

import com.ecommerce.orderservice.infrastructure.adapter.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

}

