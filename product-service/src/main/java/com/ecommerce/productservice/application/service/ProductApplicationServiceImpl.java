package com.ecommerce.productservice.application.service;

import com.ecommerce.productservice.application.dto.*;
import com.ecommerce.productservice.domain.exception.DuplicateProductNameException;
import com.ecommerce.productservice.domain.exception.InvalidInventoryException;
import com.ecommerce.productservice.domain.model.Product;
import com.ecommerce.productservice.domain.port.out.ProductEventPublisherPort;
import com.ecommerce.productservice.domain.port.out.ProductRepositoryPort;
import com.ecommerce.productservice.infrastructure.adapter.persistence.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ProductApplicationService} for managing product-related business logic.
 * <p>
 * This service handles CRUD operations, batch validation, inventory reservation, and event publishing.
 * It delegates persistence to {@link ProductRepositoryPort}, event publishing to {@link ProductEventPublisherPort},
 * and uses {@link ProductMapper} for DTO conversions. All database operations are transactional.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductApplicationServiceImpl implements ProductApplicationService {

    private final ProductRepositoryPort productRepositoryPort;
    private final ProductEventPublisherPort eventPublisherPort;
    private final ProductMapper mapper;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepositoryPort.existsByName(request.name())) {
            throw new DuplicateProductNameException(request.name());
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
    public ProductBatchValidationResponse verifyAndGetProducts(ProductBatchValidationRequest request) {
        // Fetch products in one query
        List<Long> productIds = request.items().stream().map(ProductBatchItemRequest::productId).toList();
        Map<Long, Product> productMap = productRepositoryPort.findAllByIds(productIds)
                .stream()
                .collect(Collectors.toMap(Product::id, p -> p));
        // Process each item
        List<ProductBatchItemResponse> responses = request.items().stream().map(item -> {
            Product product = productMap.get(item.productId());
            if (product == null) {
                return new ProductBatchItemResponse(item.productId(), null, null, false, 0, "Product not found");
            }
            boolean isAvailable = product.inventory() >= item.quantity();
            String error = isAvailable ? null : "Insufficient stock. Available: " + product.inventory();
            return new ProductBatchItemResponse(
                    item.productId(),
                    product.name(),
                    product.price(),
                    isAvailable,
                    product.inventory(),
                    error
            );
        }).toList();
        return new ProductBatchValidationResponse(responses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductBatchDetailsResponse> findProductDetails(ProductBatchDetailsRequest request) {
        Map<Long, Product> productMap = productRepositoryPort.findAllByIds(request.productIds())
                .stream()
                .collect(Collectors.toMap(Product::id, p -> p));
        return request.productIds().stream()
                .map(id -> {
                    Product product = productMap.get(id);
                    if (product == null) {
                        return new ProductBatchDetailsResponse(null, String.format("Product not found with ID: %s", id));
                    }
                    return new ProductBatchDetailsResponse(mapper.toResponse(product), null);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> findByColor(String color, int page, int size) {
        Page<Product> productsPage = productRepositoryPort.findByColor(color, page, size);
        return productsPage.map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void validateAndReserveInventory(ProductBatchValidationRequest request, UUID orderId) {
        try {
            ProductBatchValidationResponse validationResponse = verifyAndGetProducts(request);
            boolean allAvailable = validationResponse.products().stream().allMatch(ProductBatchItemResponse::isAvailable);
            if (!allAvailable) {
                log.warn("Inventory validation failed for order ID: {}", orderId);
                eventPublisherPort.publishValidationFailed(orderId);
                throw new InvalidInventoryException("Inventory validation failed for order ID: %s", orderId);
            }

            List<Product> products = productRepositoryPort.findAllByIds(
                    request.items().stream().map(ProductBatchItemRequest::productId).toList()
            );
            products.forEach(product -> {
                var item = request.items().stream()
                        .filter(i -> i.productId().equals(product.id()))
                        .findFirst()
                        .orElseThrow(() -> new InvalidInventoryException("Product not found: %s", product.id()));
                if (product.inventory() < item.quantity()) {
                    throw new InvalidInventoryException("Insufficient inventory for product %s: %d available", product.name(), product.inventory());
                }
                Product refreshProduct = product.withInventory(item.quantity());
                productRepositoryPort.update(product.id(), refreshProduct);
            });

            eventPublisherPort.publishValidationSucceeded(orderId);
            log.info("Inventory reserved successfully for order ID: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to validate and reserve inventory for order ID: {}", orderId, e);
            eventPublisherPort.publishValidationFailed(orderId);
            throw e;
        }
    }
}

