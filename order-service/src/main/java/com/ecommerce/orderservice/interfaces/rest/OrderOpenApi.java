package com.ecommerce.orderservice.interfaces.rest;

import com.ecommerce.orderservice.application.dto.OrderPageResponse;
import com.ecommerce.orderservice.application.dto.OrderRequest;
import com.ecommerce.orderservice.application.dto.OrderResponse;
import com.ecommerce.shared.interfaces.openapi.response.ApiAuthErrors;
import com.ecommerce.shared.interfaces.openapi.response.ApiCommonErrors;
import com.ecommerce.shared.interfaces.openapi.response.ApiResourceNotFound;
import com.ecommerce.shared.interfaces.openapi.response.ApiValidationErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * OpenAPI interface for order endpoints.
 * <p>
 * This interface defines the API documentation for Swagger, including Spring annotations
 * to establish the contract for all endpoints.
 */
@ApiCommonErrors
@ApiAuthErrors
@RequestMapping("/orders")
public interface OrderOpenApi {

    @ApiValidationErrors
    @Operation(summary = "Create Order", description = "Creates a new order", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "201", description = "Order created successfully", content = {@Content(mediaType = "application/json",
            schema = @Schema(implementation = OrderResponse.class))})
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    OrderResponse createOrder(
            @Parameter(description = "Order details to create", required = true)
            @RequestBody OrderRequest request);

    @Operation(summary = "Retrieve all Orders", description = "Retrieve all orders with pagination", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = OrderPageResponse.class)))
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    OrderPageResponse getOrders(
            @Parameter(description = "Page number for pagination", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") int size);

    @ApiResourceNotFound
    @Operation(summary = "Order Details", description = "Retrieves the details of an order by order ID", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "200", description = "Success", content = {@Content(mediaType = "application/json",
            schema = @Schema(implementation = OrderResponse.class))})
    @GetMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    OrderResponse getOrderById(
            @Parameter(description = "ID of the order to retrieve", required = true)
            @PathVariable UUID orderId);

    @ApiValidationErrors
    @ApiResourceNotFound
    @Operation(summary = "Update Order", description = "Updates an existing order", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "200", description = "Order updated successfully", content = {@Content(mediaType = "application/json",
            schema = @Schema(implementation = OrderResponse.class))})
    @PutMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    OrderResponse updateOrder(
            @Parameter(description = "ID of the order to update", required = true)
            @PathVariable UUID orderId,
            @Parameter(description = "Updated order details", required = true)
            @RequestBody OrderRequest request);

    @ApiResourceNotFound
    @Operation(summary = "Delete Order", description = "Delete an order", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "204", description = "Order deleted successfully")
    @DeleteMapping(value = "/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteOrder(
            @Parameter(description = "ID of the order to delete", required = true)
            @PathVariable UUID orderId);

    @ApiResourceNotFound
    @Operation(summary = "Cancel Order", description = "Cancel an order", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "200", description = "Success", content = {@Content(mediaType = "application/json",
            schema = @Schema(implementation = OrderResponse.class))})
    @PostMapping(value = "/{orderId}/cancel")
    void cancelOrder(
            @Parameter(description = "ID of the order to cancel", required = true)
            @PathVariable UUID orderId);
}