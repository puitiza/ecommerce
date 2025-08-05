package com.ecommerce.orderservice.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductAvailabilityResponse {
    private boolean isAvailable;
    private Integer availableUnits;
}
