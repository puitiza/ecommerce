package com.ecommerce.orderservice.interfaces.rest;

import com.ecommerce.orderservice.application.dto.OrderPageResponse;
import com.ecommerce.orderservice.application.dto.OrderRequest;
import com.ecommerce.orderservice.application.dto.OrderResponse;
import com.ecommerce.orderservice.application.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for handling order-related HTTP requests.
 * This class delegates all business logic to the {@link OrderService}.
 */
@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController implements OrderOpenApi {

    private final OrderService orderService;

    @Override
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {
        log.info("Creating order: {}", request);
        return orderService.createOrder(request);
    }

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderPageResponse getOrders(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        log.info("Retrieving orders for page {} with size {}", page, size);
        return new OrderPageResponse(orderService.getAllOrders(page, size));
    }

    @Override
    @GetMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse getOrderById(@PathVariable UUID orderId) {
        log.info("Retrieving order with ID: {}", orderId);
        return orderService.getOrderById(orderId);
    }

    @Override
    @PutMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse updateOrder(@PathVariable UUID orderId, @Valid @RequestBody OrderRequest request) {
        log.info("Updating order with ID: {} with request: {}", orderId, request);
        return orderService.updateOrder(orderId, request);
    }

    @Override
    @DeleteMapping(value = "/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable UUID orderId) {
        log.info("DELETING ORDER WITH ID {}", orderId);
        orderService.deleteOrder(orderId);
    }

    @Override
    @PostMapping(value = "/{orderId}/cancel")
    public void cancelOrder(@PathVariable UUID orderId) {
        log.info("Cancelling order with ID: {}", orderId);
        orderService.cancelOrder(orderId);
    }

    @GetMapping("/orders/search")
    public String testBoolean(@RequestParam boolean active) {
        return "Active: " + active;
    }
}
