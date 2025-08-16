package com.ecommerce.productservice.service;

import com.ecommerce.productservice.configuration.exception.handler.InvalidInventoryException;
import com.ecommerce.productservice.configuration.exception.handler.ProductUpdateException;
import com.ecommerce.productservice.model.dto.*;
import com.ecommerce.productservice.model.entity.ProductEntity;
import com.ecommerce.productservice.repository.ProductRepository;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ModelMapper modelMapper;

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        // Convert ProductDto into Product JPA Entity
        var productEntity = modelMapper.map(productDto, ProductEntity.class);
        var savedProduct = repository.save(productEntity);

        // Convert Product JPA entity to ProductDto
        return modelMapper.map(savedProduct, ProductDto.class);
    }

    @Override
    public List<ProductDto> getAllProducts() {
        var products = repository.findAll();
        return products.stream()
                .map((product) -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public ProductDto getProductById(Long productId) {
        var productEntity = repository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));
        return modelMapper.map(productEntity, ProductDto.class);
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto ProductEntity) {
        var existingProductDto = getProductById(id);
        var existingProduct = modelMapper.map(existingProductDto, ProductEntity.class);

        existingProduct.setName(ProductEntity.getName());
        existingProduct.setDescription(ProductEntity.getDescription());
        existingProduct.setPrice(ProductEntity.getPrice());
        existingProduct.setInventory(ProductEntity.getInventory());
        existingProduct.setImage(ProductEntity.getImage());
        existingProduct.setCategories(ProductEntity.getCategories());
        //existingProduct.setAdditionalData(ProductEntity.getAdditionalData());

        var updatedProduct = repository.save(existingProduct);

        return modelMapper.map(updatedProduct, ProductDto.class);
    }

    @Override
    public void deleteProduct(Long id) {
        repository.deleteById(id);
    }

    @Override
    public ProductAvailabilityDto verifyProductAvailability(Long productId, Integer quantity) {
        var productEntity = repository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));
        Integer availableQuantity = productEntity.getInventory();
        return new ProductAvailabilityDto((availableQuantity >= quantity), availableQuantity);
    }

    @Override
    public void updateProductInventory(Long productId, Integer updatedInventory) {
        log.info("Updating inventory for product with ID {} to {}", productId, updatedInventory);

        // Input validation
        if (updatedInventory < 0) {
            throw new InvalidInventoryException("Inventory cannot be negative");
        }

        try {
            var productEntity = repository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));
            // Save updated product
            productEntity.setInventory(updatedInventory);
            repository.save(productEntity);

            log.info("Inventory updated successfully");
        } catch (DataAccessException ex) {
            log.error("Failed to update inventory: {}", ex.getMessage());
            throw new ProductUpdateException("Failed to update product inventory", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BatchProductResponse verifyAndGetProducts(BatchProductRequest request) {
        // Fetch products in one query
        List<Long> productIds = request.items().stream().map(BatchProductItemRequest::productId).toList();
        List<ProductEntity> products = repository.findAllByIdIn(productIds);
        Map<Long, ProductEntity> productMap = products.stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        // Process each item
        List<BatchProductItemResponse> responses = request.items().stream().map(item -> {
            var product = productMap.get(item.productId());
            if (product == null) {
                return new BatchProductItemResponse(item.productId(), null, null, false, 0, "Product not found");
            }
            boolean isAvailable = product.getInventory() >= item.quantity();
            String error = isAvailable ? null : "Insufficient stock. Available: " + product.getInventory();
            return new BatchProductItemResponse(
                    item.productId(),
                    product.getName(),
                    product.getPrice(),
                    isAvailable,
                    product.getInventory(),
                    error
            );
        }).toList();

        return new BatchProductResponse(responses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchProductDetailsResponse> getProductDetails(BatchProductDetailsRequest request) {
        List<Long> productIds = request.productIds();
        List<ProductEntity> products = repository.findAllByIdIn(productIds);
        Map<Long, ProductEntity> productMap = products.stream()
                .collect(Collectors.toMap(ProductEntity::getId, p -> p));

        return productIds.stream()
                .map(id -> {
                    ProductEntity entity = productMap.get(id);
                    if (entity == null) {
                        return new BatchProductDetailsResponse(null, String.format("Product not found with ID: %s", id));
                    }
                    return new BatchProductDetailsResponse(modelMapper.map(entity, ProductDto.class), null);
                })
                .toList();
    }

}

