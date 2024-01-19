package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.controller.openApi.OrderOpenApi;
import com.ecommerce.orderservice.model.dto.OrderDto;
import com.ecommerce.orderservice.model.request.CreateOrderRequest;
import com.ecommerce.orderservice.model.request.UpdateOrderRequest;
import com.ecommerce.orderservice.services.OrderService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
    public Page<OrderDto> getOrders(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        log.info("RETRIEVING ORDERS FOR PAGE {} WITH SIZE {}", page, size);
        return orderService.getAllOrders(page, size);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderDto getOrderById(@PathVariable Long id) {
        log.info("GETTING ORDER WITH ID {}", id);
        return orderService.getOrderById(id);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderDto updateOrder(@PathVariable Long id, @Valid @RequestBody UpdateOrderRequest request) {
        log.info("UPDATING ORDER WITH ID {}: {}", id, request);
        return orderService.updateOrder(id, request);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable Long id) {
        log.info("DELETING ORDER WITH ID {}", id);
        orderService.cancelOrder(id);
    }
}
