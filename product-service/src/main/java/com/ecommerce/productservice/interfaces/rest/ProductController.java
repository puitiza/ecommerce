package com.ecommerce.productservice.interfaces.rest;

import com.ecommerce.productservice.application.dto.*;
import com.ecommerce.productservice.domain.port.in.ProductUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for handling product-related HTTP requests.
 * Implements the {@link ProductOpenApi} interface to provide CRUD operations,
 * batch processing, and color-based queries for products.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ProductController implements ProductOpenApi {

    private final ProductUseCase productUseCase;

    @Override
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        log.info("Creating product: {}", request);
        return productUseCase.create(request);
    }

    @Override
    public ProductResponse findById(@PathVariable Long id) {
        log.info("Retrieving product with ID: {}", id);
        return productUseCase.findById(id);
    }

    @Override
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        log.info("Updating product with ID: {} with request: {}", id, request);
        return productUseCase.update(id, request);
    }

    @Override
    public void delete(@PathVariable Long id) {
        log.info("Deleting product with ID: {}", id);
        productUseCase.delete(id);
    }

    @Override
    public ProductPageResponse findAllPaginated(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        log.info("Retrieving product for page {} with size {}", page, size);
        Page<ProductResponse> productsPage = productUseCase.findAllPaginated(page, size);
        return new ProductPageResponse(productsPage);
    }

    @Override
    public BatchProductResponse verifyAndGetProducts(@Valid @RequestBody BatchProductRequest request) {
        log.info("Verify Products in Batch: {}", request.toString());
        return productUseCase.verifyAndGetProducts(request);
    }

    @Override
    public List<BatchProductDetailsResponse> findProductDetailsByIds(@Valid @RequestBody BatchProductDetailsRequest request) {
        log.info("Retrieving products in Batch: {}", request.productIds());
        return productUseCase.findProductDetails(request);
    }

    @Override
    public ProductPageResponse findByColor(@PathVariable String color,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size) {
        log.info("Retrieving products with color {} for page {} with size {}", color, page, size);
        var paginatedList = productUseCase.findByColor(color, page, size);
        return new ProductPageResponse(paginatedList);
    }

}
