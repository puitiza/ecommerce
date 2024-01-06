package com.ecommerce.productservice.service;

import com.ecommerce.productservice.model.entity.ProductEntity;
import com.ecommerce.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public record ProductServiceImpl(ProductRepository repository) implements ProductService {

    @Override
    public ProductEntity createProduct(ProductEntity ProductEntity) {
        return repository.save(ProductEntity);
    }

    @Override
    public List<ProductEntity> getAllProducts() {
        return repository.findAll();
    }

    @Override
    public ProductEntity getProductById(Long productId) {
        Optional<ProductEntity> productOptional = repository.findById(productId);
        return productOptional.orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
    }

    @Override
    public ProductEntity updateProduct(Long id, ProductEntity ProductEntity) {
        ProductEntity existingProduct = getProductById(id);
        existingProduct.setName(ProductEntity.getName());
        existingProduct.setDescription(ProductEntity.getDescription());
        existingProduct.setPrice(ProductEntity.getPrice());
        existingProduct.setInventory(ProductEntity.getInventory());
        existingProduct.setImage(ProductEntity.getImage());
        existingProduct.setCategories(ProductEntity.getCategories());
        existingProduct.setAdditionalData(ProductEntity.getAdditionalData());
        return repository.save(existingProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        repository.deleteById(id);
    }

}

