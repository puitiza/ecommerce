package com.ecommerce.productservice.model.dto;

import java.util.List;

public record BatchProductResponse(List<BatchProductItemResponse> products) {
}

