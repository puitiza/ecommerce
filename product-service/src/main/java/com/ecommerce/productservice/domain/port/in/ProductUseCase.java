package com.ecommerce.productservice.domain.port.in;

import com.ecommerce.productservice.application.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductUseCase {

    ProductResponse create(ProductRequest request);

    Page<ProductResponse> findAllPaginated(int page, int size);

    ProductResponse findById(Long id);

    ProductResponse update(Long id, ProductRequest request);

    void delete(Long id);

    BatchProductResponse verifyAndGetProducts(BatchProductRequest request);

    List<BatchProductDetailsResponse> findProductDetails(BatchProductDetailsRequest request);

    Page<ProductResponse> findByColor(String color, int page, int size);

}
