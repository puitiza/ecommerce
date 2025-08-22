package com.ecommerce.productservice.domain.port.in;

import com.ecommerce.productservice.application.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductUseCase {

    ProductResponse create(ProductRequest request);

    Page<ProductResponse> getAllPaginatedList(int page, int size);

    ProductResponse getProductById(Long id);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);

    BatchProductResponse verifyAndGetProducts(BatchProductRequest request);

    List<BatchProductDetailsResponse> getProductDetails(BatchProductDetailsRequest request);

    Page<ProductResponse> findByColor(String color, int page, int size);

}
