package com.ecommerce.productservice.controller.openApi;

import com.ecommerce.productservice.model.dto.ProductDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@SuppressWarnings("ALL")
public interface ProductOpenApi {


    @Operation(summary = "Create Product", description = "Creates a new product",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    ProductDto createProduct(ProductDto product);

    @Operation(summary = "Retrieve all Products", description = "Retrieve all products",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    List<ProductDto> getProducts();

    @Operation(summary = "Product Details", description = "Retrieves the details of a Product by ProductId",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success", content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Server Error")
    })
    ProductDto getProductById(Long id);


    @Operation(summary = "Update Product", description = "Updates an existing product",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product update data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    ProductDto updateProduct(Long id, ProductDto product);

    @Operation(summary = "Delete Product", description = "Deletes a product",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    void deleteProduct(Long id);
}
