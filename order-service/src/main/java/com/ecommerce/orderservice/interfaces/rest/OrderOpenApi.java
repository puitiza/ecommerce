package com.ecommerce.orderservice.interfaces.rest;

import com.ecommerce.orderservice.application.dto.OrderPageResponse;
import com.ecommerce.orderservice.application.dto.OrderRequest;
import com.ecommerce.orderservice.application.dto.OrderResponse;
import com.ecommerce.shared.interfaces.openapi.CrudOpenApi;
import com.ecommerce.shared.interfaces.openapi.ListableCrudOpenApi;
import com.ecommerce.shared.interfaces.openapi.response.ApiResourceNotFound;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * OpenAPI interface for order endpoints.
 * Extends CrudOpenApi and ListableCrudOpenApi for standard CRUD operations and adds order-specific endpoints.
 */
@RequestMapping("/orders")
public interface OrderOpenApi extends CrudOpenApi<OrderResponse, OrderRequest, UUID>, ListableCrudOpenApi<OrderPageResponse> {

    @Override
    @Operation(summary = "Create Order", description = "Creates a new order", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "201", description = "Order created successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = OrderResponse.class)))
    OrderResponse create(@Parameter(description = "Order details to create", required = true)
                         @RequestBody OrderRequest request);

    @Override
    @Operation(summary = "Retrieve All Orders", description = "Retrieves all orders with pagination", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = OrderPageResponse.class)))
    OrderPageResponse findAllPaginated(@Parameter(description = "Page number for pagination", example = "0")
                             @RequestParam(defaultValue = "0") int page,
                                       @Parameter(description = "Number of items per page", example = "10")
                             @RequestParam(defaultValue = "10") int size);

    @Override
    @Operation(summary = "Retrieve Order by ID", description = "Retrieves the details of an order by its ID", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "200", description = "Order retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = OrderResponse.class)))
    OrderResponse findById(@Parameter(description = "ID of the order to retrieve", required = true)
                          @PathVariable("id") UUID id);

    @Override
    @Operation(summary = "Update Order", description = "Updates an existing order by its ID", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "200", description = "Order updated successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = OrderResponse.class)))
    OrderResponse update(@Parameter(description = "ID of the order to update", required = true)
                         @PathVariable("id") UUID id,
                         @Parameter(description = "Updated order details", required = true)
                         @RequestBody OrderRequest request);

    @Override
    @Operation(summary = "Delete Order", description = "Deletes an order by its ID", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "204", description = "Order deleted successfully")
    void delete(@Parameter(description = "ID of the order to delete", required = true)
                @PathVariable("id") UUID id);

    @ApiResourceNotFound
    @Operation(summary = "Cancel Order", description = "Cancels an order by its ID", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "204", description = "Order cancelled successfully")
    @DeleteMapping(value = "/{id}/cancel")
    void cancelOrder(@Parameter(description = "ID of the order to cancel", required = true)
                     @PathVariable("id") UUID id);
}