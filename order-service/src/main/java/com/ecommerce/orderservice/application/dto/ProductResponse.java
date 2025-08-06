package com.ecommerce.orderservice.application.dto;

public record ProductResponse(
        Long id,
        String name,
        Double price,
        Integer inventory
) {}