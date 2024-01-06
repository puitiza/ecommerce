package com.ecommerce.productservice.controller;


import com.ecommerce.productservice.controller.openApi.ProductOpenApi;
import com.ecommerce.productservice.model.entity.ProductEntity;
import com.ecommerce.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/products")
public record ProductController(ProductService productService) implements ProductOpenApi {

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProductEntity createProduct(@Valid @RequestBody ProductEntity product) {
        log.info("CREATING PRODUCT: {}", product);
        return productService.createProduct(product);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductEntity> getProducts() {
        log.info("GETTING ALL PRODUCTS");
        return productService.getAllProducts();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductEntity getProductById(@PathVariable Long id) {
        log.info("GETTING PRODUCT WITH ID {}", id);
        return productService.getProductById(id);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductEntity updateProduct(@PathVariable Long id, @Valid @RequestBody ProductEntity product) {
        log.info("UPDATING PRODUCT WITH ID {}: {}", id, product);
        return productService.updateProduct(id, product);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        log.info("DELETING PRODUCT WITH ID {}", id);
        productService.deleteProduct(id);
    }
}
