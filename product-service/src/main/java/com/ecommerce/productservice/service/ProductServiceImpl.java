package com.ecommerce.productservice.service;

import com.ecommerce.productservice.model.Product;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public record ProductServiceImpl() implements ProductService {
    @Override
    public Product getProductById(Long productId) {
        return Product.builder()
                .id(1L).name("Spring").price(0.2)
                .build();
    }

    @Override
    public List<Product> getAllProducts() {
        return Arrays.asList(
                new Product(1L, "Spring", 0.2),
                new Product(2L, "Spring Cloud Eureka Service Discovery", 0.2),
                new Product(3L, "Spring Cloud Eureka Client", 0.5)
        );
    }
}
