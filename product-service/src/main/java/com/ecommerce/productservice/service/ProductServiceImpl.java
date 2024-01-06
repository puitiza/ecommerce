package com.ecommerce.productservice.service;

import com.ecommerce.productservice.configuration.exception.handler.NoSuchElementFoundException;
import com.ecommerce.productservice.model.dto.ProductDto;
import com.ecommerce.productservice.model.entity.ProductEntity;
import com.ecommerce.productservice.model.mapper.ProductMapper;
import com.ecommerce.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public record ProductServiceImpl(ProductRepository repository, ProductMapper productMapper) implements ProductService {

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        var productEntity = productMapper.toProductEntity(productDto);
        return productMapper.toProductDto(repository.save(productEntity));
    }

    @Override
    public List<ProductDto> getAllProducts() {
        var list = repository.findAll();
        return productMapper.mapProductDtoList(list);
    }

    @Override
    public ProductDto getProductById(Long productId) {
        Optional<ProductEntity> productOptional = repository.findById(productId);
        var productEntity = productOptional
                .orElseThrow(() -> new NoSuchElementFoundException("Product not found with ID: " + productId, "P01"));
        return productMapper.toProductDto(productEntity);
    }

    @Override
    public ProductDto updateProduct(Long id, ProductDto ProductEntity) {

        var existingProductDto = getProductById(id);
        var existingProduct = productMapper.toProductEntity(existingProductDto);

        existingProduct.setName(ProductEntity.getName());
        existingProduct.setDescription(ProductEntity.getDescription());
        existingProduct.setPrice(ProductEntity.getPrice());
        existingProduct.setInventory(ProductEntity.getInventory());
        existingProduct.setImage(ProductEntity.getImage());
        existingProduct.setCategories(ProductEntity.getCategories());
        //existingProduct.setAdditionalData(ProductEntity.getAdditionalData());
        return productMapper.toProductDto(repository.save(existingProduct));

    }

    @Override
    public void deleteProduct(Long id) {
        repository.deleteById(id);
    }

}

