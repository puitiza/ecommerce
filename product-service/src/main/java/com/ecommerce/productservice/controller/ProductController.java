package com.ecommerce.productservice.controller;


import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/products")
@RestController
public record ProductController(ProductService productService) {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    @GetMapping("/{productId}")
    public Product getProductById(@PathVariable Long productId) {
        logger.info("GETTING PRODUCT WITH ID {}", productId);
        return productService.getProductById(productId);
    }

    @GetMapping("/all")
    public List<Product> getAllProducts() {
        logger.info("GETTING ALL PRODUCTS");
        return productService.getAllProducts();
    }

    @GetMapping("/byOrderId")
    public List<Product> getProductsByOrderId(@RequestParam Long orderId) {
        logger.info("COLLECTING LIST PRODUCT BY ORDER_ID {} FROM UPSTREAM SERVICE", orderId);
        return productService.getProductsByOrderId(orderId);
    }
}
