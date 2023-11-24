package com.ecommerce.productservice.controller;


import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.service.ProductService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/products")
@RestController
public record ProductController(ProductService productService) {

    @GetMapping("/{productId}")
    public Product getProductById(@PathVariable Long productId) {
        return productService.getProductById(productId);
    }

    @GetMapping("/all")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/byOrderId")
    public List<Product> getProductsByOrderId(@RequestParam Long orderId) {
        return productService.getProductsByOrderId(orderId);
    }
}
