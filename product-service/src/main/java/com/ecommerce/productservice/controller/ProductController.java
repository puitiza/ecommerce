package com.ecommerce.productservice.controller;


import com.ecommerce.productservice.model.Product;
import com.ecommerce.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/product")
@RestController
public record ProductController(ProductService productService) {

    @Operation(summary = "Product Details",  description = "Retrieves the details of a Product by ProductId",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode="200", description ="Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    @GetMapping("/{productId}")
    public Product getProductById(@PathVariable Long productId) {
        log.info("GETTING PRODUCT WITH ID {}", productId);
        return productService.getProductById(productId);
    }

    @GetMapping("/all")
    public List<Product> getAllProducts() {
        log.info("GETTING ALL PRODUCTS");
        return productService.getAllProducts();
    }

    @GetMapping("/byOrderId")
    public List<Product> getProductsByOrderId(@RequestParam Long orderId) {
        log.info("COLLECTING LIST PRODUCT BY ORDER_ID {} FROM UPSTREAM SERVICE", orderId);
        return productService.getProductsByOrderId(orderId);
    }
}
