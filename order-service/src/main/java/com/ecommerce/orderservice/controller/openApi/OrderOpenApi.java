package com.ecommerce.orderservice.controller.openApi;

import com.ecommerce.orderservice.model.dto.OrderDto;
import com.ecommerce.orderservice.model.request.CreateOrderRequest;
import com.ecommerce.orderservice.model.request.UpdateOrderRequest;
import com.ecommerce.shared.openapi.ResponseApiTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;

import java.util.UUID;

@SuppressWarnings("unused")
public interface OrderOpenApi {

    @Operation(summary = "Create Order", description = "Creates a new order", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = OrderDto.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.UNAUTHORIZED))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.FORBIDDEN))),
            @ApiResponse(responseCode = "422", description = "Invalid order data request",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.UNPROCESSABLE)))
    })
    OrderDto createOrder(CreateOrderRequest request);

    @Operation(summary = "Retrieve all Orders", description = "Retrieve all orders", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                    content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OrderDto.class)))})
    })
    Page<OrderDto> getOrders(int page, int size);

    @Operation(summary = "Order Details", description = "Retrieves the details of an order by order ID", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Success",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = OrderDto.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
    })
    OrderDto getOrderById(UUID id);

    @Operation(summary = "Update Order", description = "Updates an existing order", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order updated successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = OrderDto.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
            @ApiResponse(responseCode = "422", description = "Invalid order update data", content = @Content)
    })
    OrderDto updateOrder(UUID id, UpdateOrderRequest request);

    @Operation(summary = "Delete Order", description = "Delete an order", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    void deleteOrder(UUID id);
}

