package com.ecommerce.productservice.application.service;

import com.ecommerce.productservice.application.dto.*;

import java.util.List;

public interface ProductApplicationService {
    ProductDto createProduct(ProductDto product);

    List<ProductDto> getAllProducts();

    ProductDto getProductById(Long id);

    ProductDto updateProduct(Long id, ProductDto product);

    void deleteProduct(Long id);

    ProductAvailabilityDto verifyProductAvailability(Long productId, Integer quantity);

    void updateProductInventory(Long id, Integer updatedInventory);

    BatchProductResponse verifyAndGetProducts(BatchProductRequest request);

    List<BatchProductDetailsResponse> getProductDetails(BatchProductDetailsRequest request);

}
