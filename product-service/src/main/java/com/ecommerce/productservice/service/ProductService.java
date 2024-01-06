package com.ecommerce.productservice.service;

import com.ecommerce.productservice.model.entity.ProductEntity;

import java.util.List;

public interface ProductService {
    ProductEntity createProduct(ProductEntity product);

    List<ProductEntity> getAllProducts();

    ProductEntity getProductById(Long id);

    ProductEntity updateProduct(Long id, ProductEntity product);

    void deleteProduct(Long id);

}
