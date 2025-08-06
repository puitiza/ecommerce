package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
public class OrderDomainServiceImpl implements OrderDomainService {
    private static final Set<OrderStatus> CANCELLABLE_STATES = Set.of(
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.PROCESSING
    );

    @Override
    public boolean canCancel(Order order) {
        return CANCELLABLE_STATES.contains(order.status());
    }

    @Override
    public Order calculateTotalPrice(Order order) {
        BigDecimal totalPrice = order.items().stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return order.withItemsAndTotalPrice(order.items(), totalPrice);
    }
}