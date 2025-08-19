package com.ecommerce.orderservice.infrastructure.adapter.persistence.repository;

import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.port.out.OrderRepositoryPort;
import com.ecommerce.orderservice.infrastructure.adapter.persistence.mapper.OrderMapper;
import com.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter that implements the OrderRepositoryPort.
 * It uses Spring Data JPA to interact with the database and maps between
 * domain models and persistence entities.
 */
@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository jpaRepository;
    private final OrderMapper mapper;

    @Override
    public Order save(Order order) {
        var orderEntity = mapper.toEntity(order);
        var savedEntity = jpaRepository.save(orderEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Order findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id.toString()));
    }

    @Override
    public Page<Order> findAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        var entities = jpaRepository.findAll(pageable);
        return new PageImpl<>(entities.getContent().stream().map(mapper::toDomain).toList(),
                pageable, entities.getTotalElements());
    }
}
