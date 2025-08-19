package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.controller.openApi.ProductOpenApi;
import com.ecommerce.productservice.model.dto.*;
import com.ecommerce.productservice.model.request.OrderItemRequest;
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

    @Override
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDto create(@Valid @RequestBody ProductDto resource) {
        log.info("CREATING PRODUCT: {}", resource);
        return productService.createProduct(resource);
    }

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProductDto> getAll() {
        log.info("GETTING ALL PRODUCTS");
        return productService.getAllProducts();
    }

    @Override
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductDto getById(@PathVariable Long id) {
        log.info("GETTING PRODUCT WITH ID {}", id);
        return productService.getProductById(id);
    }

    @Override
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductDto update(@PathVariable Long id, @Valid @RequestBody ProductDto product) {
        log.info("UPDATING PRODUCT WITH ID {}: {}", id, product);
        return productService.updateProduct(id, product);
    }

    @Override
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("DELETING PRODUCT WITH ID {}", id);
        productService.deleteProduct(id);
    }

    @Override
    @PostMapping("/verify-availability")
    public ProductAvailabilityDto verifyProductAvailability(@Valid @RequestBody OrderItemRequest orderItemRequest) {
        log.info("VERIFYING AVAILABILITY FOR PRODUCT: {}", orderItemRequest.getProductId());
        return productService.verifyProductAvailability(orderItemRequest.getProductId(), orderItemRequest.getQuantity());
    }

    @Override
    @PutMapping("/update-inventory/{id}/{updatedInventory}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProductInventory(@PathVariable Long id, @PathVariable Integer updatedInventory) {
        log.info("UPDATING INVENTORY FOR PRODUCT WITH ID {}: {}", id, updatedInventory);
        productService.updateProductInventory(id, updatedInventory);
    }

    @PostMapping(value = "/batch", produces = MediaType.APPLICATION_JSON_VALUE)
    public BatchProductResponse verifyAndGetProducts(@Valid @RequestBody BatchProductRequest request) {
        log.info("Verify Products in Batch : {}", request.toString());
        return productService.verifyAndGetProducts(request);
    }

    @PostMapping(value = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<BatchProductDetailsResponse> getProductDetails(@Valid @RequestBody BatchProductDetailsRequest request) {
        log.info("Get Products in Batch : {}", request.toString());
        return productService.getProductDetails(request);
    }

}
