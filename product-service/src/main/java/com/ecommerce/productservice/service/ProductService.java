package com.ecommerce.productservice.service;

import com.ecommerce.productservice.model.dto.*;

import java.util.List;

public interface ProductService {
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
