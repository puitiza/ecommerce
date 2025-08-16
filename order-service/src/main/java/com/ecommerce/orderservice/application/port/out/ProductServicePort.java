package com.ecommerce.orderservice.application.port.out;

import com.ecommerce.orderservice.application.dto.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface ProductServicePort {
    ProductResponse getProductById(Long id, String token);

    ProductAvailabilityResponse verifyProductAvailability(OrderItemRequest orderItemRequest, String token);

    void updateProductInventory(Long id, int updatedInventory, String token);

    BatchProductResponse verifyAndGetProducts(BatchProductRequest items, String token);

    List<BatchProductDetailsResponse> getProductsDetailsInBatch(BatchProductDetailsRequest request, String token);

}