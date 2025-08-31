package com.ecommerce.productservice.application.dto;

import java.util.List;

public record ProductBatchValidationResponse(List<ProductBatchItemResponse> products) {
}

