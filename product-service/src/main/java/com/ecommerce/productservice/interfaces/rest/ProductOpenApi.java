package com.ecommerce.productservice.interfaces.rest;

import com.ecommerce.productservice.application.dto.*;
import com.ecommerce.shared.interfaces.openapi.CrudOpenApi;
import com.ecommerce.shared.interfaces.openapi.ListableCrudOpenApi;
import com.ecommerce.shared.interfaces.openapi.response.ApiValidationErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/products")
public interface ProductOpenApi extends CrudOpenApi<ProductResponse, ProductRequest, Long>, ListableCrudOpenApi<ProductPageResponse> {

    @Override
    @Operation(summary = "Create Product", description = "Creates a new product",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(
            responseCode = "201", description = "Product created successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProductResponse.class)))
    ProductResponse create(@Parameter(description = "Product details to create", required = true)
                           @RequestBody ProductRequest request);

    @Override
    @Operation(summary = "Retrieve Product by ID", description = "Retrieves the details of an product by its ID",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(
            responseCode = "200", description = "Product retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProductResponse.class)))
    ProductResponse findById(@Parameter(description = "ID of the product to retrieve", required = true)
                             @PathVariable("id") Long id);

    @Override
    @Operation(summary = "Update Product", description = "Updates an existing product by its ID",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(
            responseCode = "200", description = "Product updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProductResponse.class)))
    ProductResponse update(@Parameter(description = "ID of the product to update", required = true)
                           @PathVariable("id") Long id,
                           @Parameter(description = "Updated product details", required = true)
                           @RequestBody ProductRequest request);

    @Override
    @Operation(summary = "Delete Product", description = "Deletes a product by its ID",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "204", description = "Product deleted successfully")
    void delete(@Parameter(description = "ID of the product to delete", required = true)
                @PathVariable("id") Long id);

    @Override
    @Operation(summary = "Retrieve All Products", description = "Retrieves all products with pagination",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(
            responseCode = "200", description = "Products retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProductPageResponse.class)))
    ProductPageResponse findAllPaginated(@Parameter(description = "Page number for pagination", example = "0")
                                         @RequestParam(defaultValue = "0") int page,
                                         @Parameter(description = "Number of items per page", example = "10")
                                         @RequestParam(defaultValue = "10") int size);

    @ApiValidationErrors
    @Operation(summary = "Verify and Get Products in Batch", description = "Retrieves all products verified in Inventory",
            security = @SecurityRequirement(name = "security_auth"))
    @PostMapping(value = "/batch", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    BatchProductResponse verifyAndGetProducts(@Parameter(description = "Batch product resource details", required = true)
                                              @RequestBody BatchProductRequest request);

    @ApiValidationErrors
    @Operation(summary = "Get Products in Batch", description = "Get products in Batch",
            security = @SecurityRequirement(name = "security_auth"))
    @PostMapping(value = "/details", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    List<BatchProductDetailsResponse> findProductDetailsByIds(@Parameter(description = "Batch product resource details", required = true)
                                                              @RequestBody BatchProductDetailsRequest request);

    @Operation(summary = "Get Products in Batch", description = "Get products in Batch",
            security = @SecurityRequirement(name = "security_auth"))
    @GetMapping(value = "/color/{color}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    ProductPageResponse findByColor(@Parameter(description = "Color products", required = true)
                                    @PathVariable String color,
                                    @Parameter(description = "Page number for pagination", example = "0")
                                    @RequestParam(defaultValue = "0") int page,
                                    @Parameter(description = "Number of items per page", example = "10")
                                    @RequestParam(defaultValue = "10") int size);

}