package com.ecommerce.orderservice.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//Allows the production to change or add new fields to your response without breaking consumers
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductResponse(
        Long id,
        String name,
        Double price,
        Integer inventory
) {}