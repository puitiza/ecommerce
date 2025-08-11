package com.ecommerce.orderservice.infrastructure.adapter.persistence.repository;

import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.port.OrderRepositoryPort;
import com.ecommerce.orderservice.infrastructure.adapter.persistence.mapper.OrderMapper;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderRepository;
    private final OrderMapper mapper;

    @Override
    public Order save(Order order) {
        return mapper.toDomain(orderRepository.save(mapper.toEntity(order)));
    }

    @Override
    public Order findById(UUID id) {
        return orderRepository.findById(id)
                .map(mapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id.toString()));
    }

    @Override
    public Page<Order> findAll(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        var entities = orderRepository.findAll(pageable);
        return new PageImpl<>(entities.getContent().stream().map(mapper::toDomain).toList(),
                pageable, entities.getTotalElements());
    }
}
