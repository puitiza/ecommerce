package com.ecommerce.productservice.controller.openApi;

import com.ecommerce.productservice.model.dto.ProductAvailabilityDto;
import com.ecommerce.productservice.model.dto.ProductDto;
import com.ecommerce.productservice.model.request.OrderItemRequest;
import com.ecommerce.shared.openapi.ApiErrorResponses;
import com.ecommerce.shared.openapi.CrudOpenApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

public interface ProductOpenApi extends CrudOpenApi<ProductDto, Long> {

    @Operation(summary = "Verify Product Availability", description = "Verifies if a product is available for an order",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiErrorResponses
    @ApiResponse(responseCode = "200", description = "Availability verified successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductAvailabilityDto.class)))
    ProductAvailabilityDto verifyProductAvailability(OrderItemRequest orderItemRequest);

    @Operation(summary = "Update Product Inventory", description = "Updates the inventory of a product",
            security = @SecurityRequirement(name = "security_auth"))
    @ApiErrorResponses
    @ApiResponse(responseCode = "204", description = "Inventory updated successfully")
    void updateProductInventory(Long id, Integer updatedInventory);
}