package com.ecommerce.productservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductAvailabilityDto {
    @JsonProperty("isAvailable")
    private boolean isAvailable;
    private Integer availableUnits;
}
