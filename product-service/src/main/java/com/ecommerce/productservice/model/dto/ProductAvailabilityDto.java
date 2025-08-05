package com.ecommerce.productservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductAvailabilityDto {
    private boolean isAvailable;
    private Integer availableUnits;
}
