package com.ecommerce.productservice.application.service;

import com.ecommerce.productservice.application.dto.*;
import com.ecommerce.productservice.domain.model.Product;
import com.ecommerce.productservice.domain.port.out.ProductRepositoryPort;
import com.ecommerce.productservice.infrastructure.adapter.persistence.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ProductApplicationService}, handling product-related business logic.
 * Delegates persistence operations to {@link ProductRepositoryPort} and maps DTOs using {@link ProductMapper}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductApplicationServiceImpl implements ProductApplicationService {

    private final ProductRepositoryPort productRepositoryPort;
    private final ProductMapper mapper;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepositoryPort.existsByName(request.name())) {
            throw new IllegalArgumentException("Product name already exists: " + request.name());
        }
        Product product = mapper.toProduct(request, null);
        Product savedProduct = productRepositoryPort.save(product);
        return mapper.toResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> findAllPaginated(int page, int size) {
        Page<Product> productsPage = productRepositoryPort.findAll(page, size);
        return productsPage.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = productRepositoryPort.findById(id);
        return mapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        // Validate uniqueness of the name
        productRepositoryPort.findByName(request.name())
                .filter(product -> !product.id().equals(id))
                .ifPresent(product -> {
                    throw new IllegalArgumentException("Product name already exists: " + request.name());
                });

        Product existing = productRepositoryPort.findById(id);
        Product updatedProduct = existing.updateFrom(request);
        Product savedProduct = productRepositoryPort.update(id, updatedProduct);
        return mapper.toResponse(savedProduct);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        productRepositoryPort.delete(id);
    }

    /**
     * Verifies the availability of a batch of products by checking their
     * inventory levels. It fetches all required products in a single query
     * to optimize performance, then processes each item.
     *
     * @param request The batch request containing product IDs and quantities.
     * @return A response containing a list of products with their availability status.
     */
    @Override
    @Transactional(readOnly = true)
    public BatchProductResponse verifyAndGetProducts(BatchProductRequest request) {
        // Fetch products in one query
        List<Long> productIds = request.items().stream().map(BatchProductItemRequest::productId).toList();
        Map<Long, Product> productMap = productRepositoryPort.findAllByIds(productIds)
                .stream()
                .collect(Collectors.toMap(Product::id, p -> p));
        // Process each item
        List<BatchProductItemResponse> responses = request.items().stream().map(item -> {
            Product product = productMap.get(item.productId());
            if (product == null) {
                return new BatchProductItemResponse(item.productId(), null, null, false, 0, "Product not found");
            }
            boolean isAvailable = product.inventory() >= item.quantity();
            String error = isAvailable ? null : "Insufficient stock. Available: " + product.inventory();
            return new BatchProductItemResponse(
                    item.productId(),
                    product.name(),
                    product.price(),
                    isAvailable,
                    product.inventory(),
                    error
            );
        }).toList();
        return new BatchProductResponse(responses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchProductDetailsResponse> findProductDetails(BatchProductDetailsRequest request) {
        Map<Long, Product> productMap = productRepositoryPort.findAllByIds(request.productIds())
                .stream()
                .collect(Collectors.toMap(Product::id, p -> p));
        return request.productIds().stream()
                .map(id -> {
                    Product product = productMap.get(id);
                    if (product == null) {
                        return new BatchProductDetailsResponse(null, String.format("Product not found with ID: %s", id));
                    }
                    return new BatchProductDetailsResponse(mapper.toResponse(product), null);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> findByColor(String color, int page, int size) {
        Page<Product> productsPage = productRepositoryPort.findByColor(color, page, size);
        return productsPage.map(mapper::toResponse);
    }

}

