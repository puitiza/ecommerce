package com.ecommerce.productservice.application.service;

import com.ecommerce.productservice.application.dto.*;
import com.ecommerce.productservice.domain.exception.DuplicateProductNameException;
import com.ecommerce.productservice.domain.exception.InvalidProductDataException;
import com.ecommerce.productservice.domain.model.Product;
import com.ecommerce.productservice.domain.port.out.OrderEventPublisherPort;
import com.ecommerce.productservice.domain.port.out.ProductEventPublisherPort;
import com.ecommerce.productservice.domain.port.out.ProductRepositoryPort;
import com.ecommerce.productservice.infrastructure.adapter.persistence.mapper.ProductMapper;
import com.ecommerce.shared.domain.event.OrderEventPayload;
import com.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final OrderEventPublisherPort eventPublisherPort;
    private final ProductMapper mapper;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepositoryPort.existsByName(request.name())) {
            throw new DuplicateProductNameException(request.name());
        }
        Product product = mapper.toProduct(request, null);
        Product savedProduct = productRepositoryPort.save(product);
        //eventPublisherPort.publish(savedProduct, ProductEventType.PRODUCT_INVENTORY_UPDATED);
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
        //eventPublisherPort.publish(savedProduct, ProductEventType.PRODUCT_INVENTORY_UPDATED);
        return mapper.toResponse(savedProduct);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        productRepositoryPort.delete(id);
        //eventPublisherPort.publish();
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
    public void validateAndReserveStock(OrderEventPayload payload) {
        try {
            // Extract product IDs and quantities
            Set<Long> productIds = payload.items().stream()
                    .map(OrderEventPayload.OrderItemPayload::productId)
                    .collect(Collectors.toSet());
            Map<Long, Integer> quantities = payload.items().stream()
                    .collect(Collectors.toMap(
                            OrderEventPayload.OrderItemPayload::productId,
                            OrderEventPayload.OrderItemPayload::quantity
                    ));

            // Batch fetch products
            List<Product> products = productRepositoryPort.findAllByIds(productIds.stream().toList());
            if (products.size() != productIds.size()) {
                throw new ResourceNotFoundException("One or more products not found: ", products.toString());
            }

            // Validate stock
            Map<Long, Integer> productIdToStock = products.stream()
                    .collect(Collectors.toMap(
                            Product::id,
                            product -> {
                                int requestedQuantity = quantities.getOrDefault(product.id(), 0);
                                if (product.inventory() < requestedQuantity) {
                                    throw new InvalidProductDataException(
                                            "Insufficient inventory for product ID %d: requested %d, available %d",
                                            product.id(), requestedQuantity, product.inventory()
                                    );
                                }
                                return product.inventory() - requestedQuantity;
                            }
                    ));

            // Batch update stock
            productRepositoryPort.updateStockInBatch(productIdToStock);

            // Publish inventory updated events
            //updatedProducts.forEach(product -> eventPublisher.publishProductInventoryUpdatedEvent(product.toShared()));
            eventPublisherPort.publishValidationSucceeded(payload);
            log.info("Reserved stock for order ID: {}", payload.id());
        } catch (Exception e) {
            log.error("Failed to reserve stock for order ID: {}", payload.id(), e);
            eventPublisherPort.publishValidationFailed(payload);
            throw e;
        }
    }

    @Override
    @Transactional
    public void restock(OrderEventPayload payload) {
        // Extract product IDs and quantities
        Set<Long> productIds = payload.items().stream()
                .map(OrderEventPayload.OrderItemPayload::productId)
                .collect(Collectors.toSet());
        Map<Long, Integer> quantities = payload.items().stream()
                .collect(Collectors.toMap(
                        OrderEventPayload.OrderItemPayload::productId,
                        OrderEventPayload.OrderItemPayload::quantity
                ));

        // Batch fetch products
        List<Product> products = productRepositoryPort.findAllByIds(productIds.stream().toList());
        if (products.size() != productIds.size()) {
            log.warn("One or more products not found for order ID: {}", payload.id());
        }

        // Prepare stock updates
        Map<Long, Integer> productIdToStock = products.stream()
                .collect(Collectors.toMap(
                        Product::id,
                        product -> product.inventory() + quantities.getOrDefault(product.id(), 0)
                ));

        // Batch update stock
        productRepositoryPort.updateStockInBatch(productIdToStock);

        // Publish inventory updated events
        //updatedProducts.forEach(product -> eventPublisherPort.publishProductInventoryUpdatedEvent(product.toShared()));
        log.info("Restocked products for order ID: {}", payload.id());
    }
}

