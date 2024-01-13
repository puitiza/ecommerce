package com.ecommerce.productservice.service;

import com.ecommerce.productservice.configuration.exception.handler.NoSuchElementFoundException;
import com.ecommerce.productservice.model.dto.ProductDto;
import com.ecommerce.productservice.model.entity.ProductEntity;
import com.ecommerce.productservice.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public record ProductServiceImpl(ProductRepository repository, ModelMapper modelMapper) implements ProductService {

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        // Convert ProductDto into Product JPA Entity
        var productEntity = modelMapper.map(productDto,ProductEntity.class);
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
        var existingProduct = modelMapper.map(existingProductDto,ProductEntity.class);

        existingProduct.setName(ProductEntity.getName());
        existingProduct.setDescription(ProductEntity.getDescription());
        existingProduct.setPrice(ProductEntity.getPrice());
        existingProduct.setInventory(ProductEntity.getInventory());
        existingProduct.setImage(ProductEntity.getImage());
        existingProduct.setCategories(ProductEntity.getCategories());
        //existingProduct.setAdditionalData(ProductEntity.getAdditionalData());

        var updatedProduct = repository.save(existingProduct);

        return modelMapper.map(updatedProduct,ProductDto.class);

    }

    @Override
    public void deleteProduct(Long id) {
        repository.deleteById(id);
    }

}

