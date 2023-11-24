package com.ecommerce.orderservice.services;

import com.ecommerce.orderservice.model.Order;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public record OrderServiceImpl() implements OrderService{
    @Override
    public Order getOrderById(Long orderId) {
        return Order.builder()
                .id(1L).customerName("Pedro Pascal").totalAmount(25.50)
                .build();
    }

    @Override
    public List<Order> getAllOrders() {
        return Arrays.asList(
                new Order(1L, "Pedro Pascal", 25.50),
                new Order(2L, "Mario Pascal", 10.20),
                new Order(3L, "Julio Perez", 5.00)
        );
    }
}
