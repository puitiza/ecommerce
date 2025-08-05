package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.controller.openApi.OrderOpenApi;
import com.ecommerce.orderservice.model.dto.OrderDto;
import com.ecommerce.orderservice.model.request.CreateOrderRequest;
import com.ecommerce.orderservice.model.request.UpdateOrderRequest;
import com.ecommerce.orderservice.model.response.OrderPageResponse;
import com.ecommerce.orderservice.services.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/orders")
public record OrderController(OrderService orderService) implements OrderOpenApi {

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDto createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("CREATING ORDER: {}", request);
        return orderService.createOrder(request);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderPageResponse getOrders(@RequestParam(defaultValue = "0") @Min(0) int page,
                                       @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        log.info("RETRIEVING ORDERS FOR PAGE {} WITH SIZE {}", page, size);
        orderService.getAllOrders(page, size);
        Page<OrderDto> ordersPage = orderService.getAllOrders(page, size);
        return new OrderPageResponse(ordersPage);
    }

    @GetMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderDto getOrderById(@PathVariable UUID orderId) {
        log.info("GETTING ORDER WITH ID {}", orderId);
        return orderService.getOrderById(orderId);
    }

    @PutMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderDto updateOrder(@PathVariable UUID orderId, @Valid @RequestBody UpdateOrderRequest request) {
        log.info("UPDATING ORDER WITH ID {}: {}", orderId, request);
        return orderService.updateOrder(orderId, request);
    }

    @DeleteMapping(value = "/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable UUID orderId) {
        log.info("DELETING ORDER WITH ID {}", orderId);
        orderService.cancelOrder(orderId);
    }

    @GetMapping("/orders/search")
    public String testBoolean(@RequestParam boolean active) {
        return "Active: " + active;
    }
}
