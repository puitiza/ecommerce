package com.ecommerce.orderservice.infrastructure.mapper;

import com.ecommerce.orderservice.application.dto.OrderItemResponse;
import com.ecommerce.orderservice.application.dto.OrderResponse;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderItem;
import com.ecommerce.orderservice.infrastructure.adapter.persistence.entity.OrderEntity;
import com.ecommerce.orderservice.infrastructure.adapter.persistence.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    public Order toDomain(OrderEntity entity) {
        return new Order(
                entity.getId(),
                entity.getUserId(),
                new LinkedHashSet<>(entity.getItems().stream()
                        .map(item -> new OrderItem(
                                item.getId(),
                                item.getProductId(),
                                item.getQuantity(),
                                item.getUnitPrice()
                        ))
                        .toList()),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getTotalPrice(),
                entity.getShippingAddress()
        );
    }

    public OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.id());
        entity.setUserId(order.userId());
        entity.setItems(order.items().stream()
                .map(item -> {
                    OrderItemEntity itemEntity = new OrderItemEntity();
                    itemEntity.setId(item.id());
                    itemEntity.setProductId(item.productId());
                    itemEntity.setQuantity(item.quantity());
                    itemEntity.setUnitPrice(item.unitPrice());
                    itemEntity.setOrder(entity);
                    return itemEntity;
                })
                .collect(Collectors.toSet()));
        entity.setStatus(order.status());
        entity.setCreatedAt(order.createdAt());
        entity.setUpdatedAt(order.updatedAt());
        entity.setTotalPrice(order.totalPrice());
        entity.setShippingAddress(order.shippingAddress());
        return entity;
    }

    public OrderResponse toResponse(Order order, List<OrderItemResponse> items) {
        return new OrderResponse(
                order.id().toString(),
                order.userId(),
                items,
                order.status(),
                order.createdAt(),
                order.updatedAt(),
                order.totalPrice(),
                order.shippingAddress()
        );
        /*
        OrderResponse response = new OrderResponse(
                order.id().toString(),
                order.userId(),
                order.items().stream()
                        .map(item -> new com.ecommerce.orderservice.application.dto.OrderItemResponse(
                                item.productId(),
                                "Unknown", // Name fetched externally if needed
                                item.quantity(),
                                item.unitPrice().doubleValue()
                        ))
                        .toList(),
                order.status(),
                order.createdAt(),
                order.updatedAt(),
                order.totalPrice(),
                order.shippingAddress()
        );
         */
    }


}