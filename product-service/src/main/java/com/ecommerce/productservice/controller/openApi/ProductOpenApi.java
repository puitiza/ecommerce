package com.ecommerce.productservice.controller.openApi;

import com.ecommerce.productservice.model.dto.ProductAvailabilityDto;
import com.ecommerce.productservice.model.dto.ProductDto;
import com.ecommerce.productservice.model.request.OrderItemRequest;
import com.ecommerce.shared.interfaces.openapi.CrudOpenApi;
import com.ecommerce.shared.interfaces.openapi.response.ApiValidationErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;

@SuppressWarnings("unused")
public interface ProductOpenApi extends CrudOpenApi<ProductDto, ProductDto, Long> {

    @Override
    @Operation(summary = "Create Product", description = "Creates a new product", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "201", description = "Product created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class)))
    ProductDto create(ProductDto resource);

    //@Override
    @Operation(summary = "Retrieve All Products", description = "Retrieves all products", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProductDto.class))))
    List<ProductDto> getAll();

    @Override
    @Operation(summary = "Retrieve Product by ID", description = "Retrieves a product by its ID", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "200", description = "Product retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class)))
    ProductDto getById(Long id);

    @Override
    @Operation(summary = "Update Product", description = "Updates an existing product", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "200", description = "Product updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class)))
    ProductDto update(Long id, ProductDto resource);

    @Override
    @Operation(summary = "Delete Product", description = "Deletes a product by its ID", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "204", description = "Product deleted successfully")
    void delete(Long id);

    @ApiValidationErrors
    @Operation(summary = "Verify Product Availability", description = "Verifies if a product is available for an order", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "200", description = "Availability verified successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductAvailabilityDto.class)))
    ProductAvailabilityDto verifyProductAvailability(OrderItemRequest orderItemRequest);

    @ApiValidationErrors
    @Operation(summary = "Update Product Inventory", description = "Updates the inventory of a product", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "204", description = "Inventory updated successfully")
    void updateProductInventory(Long id, Integer updatedInventory);
}