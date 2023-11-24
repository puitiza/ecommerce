package com.ecommerce.productservice.service;

import com.ecommerce.productservice.model.Product;

import java.util.List;

public interface ProductService {
    Product getProductById(Long productId);

    List<Product> getAllProducts();

    List<Product> getProductsByOrderId(Long orderId);

}
