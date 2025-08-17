package com.ecommerce.orderservice.application.port.out;

import com.ecommerce.orderservice.application.dto.BatchProductDetailsRequest;
import com.ecommerce.orderservice.application.dto.BatchProductDetailsResponse;
import com.ecommerce.orderservice.application.dto.BatchProductRequest;
import com.ecommerce.orderservice.application.dto.BatchProductResponse;

import java.util.List;

public interface ProductServicePort {

    BatchProductResponse verifyAndGetProducts(BatchProductRequest items, String token);

    List<BatchProductDetailsResponse> getProductsDetailsInBatch(BatchProductDetailsRequest request, String token);

}