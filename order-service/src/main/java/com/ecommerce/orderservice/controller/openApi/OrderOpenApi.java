package com.ecommerce.orderservice.controller.openApi;

import com.ecommerce.orderservice.model.dto.OrderDto;
import com.ecommerce.orderservice.model.request.CreateOrderRequest;
import com.ecommerce.orderservice.model.request.UpdateOrderRequest;
import com.ecommerce.shared.openapi.ResponseApiTemplate;
import com.ecommerce.shared.openapi.responses.ApiErrorCommon;
import com.ecommerce.shared.openapi.responses.ApiErrorGetResponses;
import com.ecommerce.shared.openapi.responses.ApiErrorPostResponses;
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

    @ApiErrorPostResponses
    @Operation(summary = "Create Order", description = "Creates a new order", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "201", description = "Order created successfully",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = OrderDto.class))})
    OrderDto createOrder(CreateOrderRequest request);

    @ApiErrorCommon
    @Operation(summary = "Retrieve all Orders", description = "Retrieve all orders", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
                    content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = OrderDto.class)))})
    })
    Page<OrderDto> getOrders(int page, int size);

    @ApiErrorGetResponses
    @Operation(summary = "Order Details", description = "Retrieves the details of an order by order ID", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "200", description = "Success",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = OrderDto.class))})
    OrderDto getOrderById(UUID id);

    @ApiErrorPostResponses
    @Operation(summary = "Update Order", description = "Updates an existing order", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order updated successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = OrderDto.class))}),
            @ApiResponse(responseCode = "404", description = "Payment not found",
                    content = @Content(examples = @ExampleObject(value = ResponseApiTemplate.NOT_FOUND)))
    })
    OrderDto updateOrder(UUID id, UpdateOrderRequest request);

    @ApiErrorGetResponses
    @Operation(summary = "Delete Order", description = "Delete an order", security = @SecurityRequirement(name = "security_auth"))
    @ApiResponse(responseCode = "204", description = "Order deleted successfully")
    void deleteOrder(UUID id);
}

