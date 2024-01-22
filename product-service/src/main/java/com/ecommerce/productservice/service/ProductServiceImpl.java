package com.ecommerce.productservice.service;

import com.ecommerce.productservice.configuration.exception.handler.InvalidInventoryException;
import com.ecommerce.productservice.configuration.exception.handler.NoSuchElementFoundException;
import com.ecommerce.productservice.configuration.exception.handler.ProductUpdateException;
import com.ecommerce.productservice.model.dto.ProductAvailabilityDto;
import com.ecommerce.productservice.model.dto.ProductDto;
import com.ecommerce.productservice.model.entity.ProductEntity;
import com.ecommerce.productservice.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public record ProductServiceImpl(ProductRepository repository, ModelMapper modelMapper) implements ProductService {

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
        Optional<ProductEntity> productOptional = repository.findById(productId);
        var productEntity = productOptional
                .orElseThrow(() -> new NoSuchElementFoundException("Product not found with ID: " + productId, "P01"));
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
                .orElseThrow(() -> new NoSuchElementFoundException(String.format("Product not found with ID %d", productId), "P01"));

        Integer availableQuantity = productEntity.getInventory();
        return new ProductAvailabilityDto((availableQuantity >= quantity), availableQuantity);
    }

    @Override
    public void updateProductInventory(Long id, Integer updatedInventory) {
        log.info("Updating inventory for product with ID {} to {}", id, updatedInventory);

        // Input validation
        if (updatedInventory < 0) {
            throw new InvalidInventoryException("Inventory cannot be negative");
        }

        try {
            var productEntity = repository.findById(id)
                    .orElseThrow(() -> new NoSuchElementFoundException(String.format("Product not found with ID %d", id), "P01"));
            // Save updated product
            productEntity.setInventory(updatedInventory);
            repository.save(productEntity);

            log.info("Inventory updated successfully");
        } catch (DataAccessException ex) {
            log.error("Failed to update inventory: {}", ex.getMessage());
            throw new ProductUpdateException("Failed to update product inventory", ex);
        }
    }

}

