package com.ecommerce.productservice.application.dto;

import java.util.List;

public record BatchProductResponse(List<BatchProductItemResponse> products) {
}

