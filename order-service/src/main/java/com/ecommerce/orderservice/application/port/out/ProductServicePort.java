package com.ecommerce.orderservice.application.port.out;

import com.ecommerce.orderservice.application.dto.*;

import java.util.List;

public interface ProductServicePort {

    BatchProductResponse verifyAndGetProducts(BatchProductRequest items, String token);

    List<BatchProductDetailsResponse> getProductsDetailsInBatch(BatchProductDetailsRequest request, String token);

}