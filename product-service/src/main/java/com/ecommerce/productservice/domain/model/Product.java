package com.ecommerce.productservice.domain.model;

import com.ecommerce.productservice.application.dto.ProductRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public record Product(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer inventory,
        String image,
        Set<String> categories,
        Map<String, Object> additionalData,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public Product updateFrom(ProductRequest request) {
        return new Product(
                this.id,
                request.name() != null ? request.name() : this.name,
                request.description() != null ? request.description() : this.description,
                request.price() != null ? request.price() : this.price,
                request.inventory() != null ? request.inventory() : this.inventory,
                request.image() != null ? request.image() : this.image,
                request.categories() != null ? request.categories() : this.categories,
                request.additionalData() != null ? request.additionalData() : this.additionalData,
                this.createdAt,
                LocalDateTime.now()
        );
    }
}