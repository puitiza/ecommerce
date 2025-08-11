package com.ecommerce.orderservice.domain.port;

import com.ecommerce.orderservice.domain.model.Order;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface OrderRepositoryPort {

    Order save(Order order);

    Order findById(UUID id);

    Page<Order> findAll(int page, int size);
}
