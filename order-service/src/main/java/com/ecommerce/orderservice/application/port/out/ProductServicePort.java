package com.ecommerce.orderservice.application.port.out;

import com.ecommerce.orderservice.application.dto.ProductAvailabilityResponse;
import com.ecommerce.orderservice.application.dto.ProductResponse;
import com.ecommerce.orderservice.application.request.OrderItemRequest;

public interface ProductServicePort {
    ProductResponse getProductById(Long id, String token);
    ProductAvailabilityResponse verifyProductAvailability(OrderItemRequest orderItemRequest, String token);
    void updateProductInventory(Long id, int updatedInventory, String token);
}