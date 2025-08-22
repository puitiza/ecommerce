package com.ecommerce.orderservice.interfaces.rest;

import com.ecommerce.orderservice.application.dto.OrderPageResponse;
import com.ecommerce.orderservice.application.dto.OrderRequest;
import com.ecommerce.orderservice.application.dto.OrderResponse;
import com.ecommerce.orderservice.domain.port.in.OrderUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for handling order-related HTTP requests.
 * This class implements the {@link OrderOpenApi} to inherit CRUD operations
 * Spring and OpenAPI documentation, ensuring a clean and focused implementation.
 * <p>
 * NOTE: Parameter-level annotations (e.g., {@code @Valid}, {@code @RequestBody},
 * {@code @PathVariable}, {@code @RequestParam}) are NOT inherited from the interface.
 * They must be duplicated in the implementing methods to ensure Spring correctly
 * handles request processing, binding, and validation at runtime.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class OrderController implements OrderOpenApi {

    private final OrderUseCase orderUseCase;

    @Override
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        log.info("Creating order: {}", request);
        return orderUseCase.createOrder(request);
    }

    @Override
    public OrderPageResponse findAllPaginated(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        log.info("Retrieving orders for page {} with size {}", page, size);
        return new OrderPageResponse(orderUseCase.getAllOrders(page, size));
    }

    @Override
    public OrderResponse findById(@PathVariable("id") UUID id) {
        log.info("Retrieving order with ID: {}", id);
        return orderUseCase.getOrderById(id);
    }

    @Override
    public OrderResponse update(@PathVariable("id") UUID id, @Valid @RequestBody OrderRequest request) {
        log.info("Updating order with ID: {} with request: {}", id, request);
        return orderUseCase.updateOrder(id, request);
    }

    @Override
    public void delete(@PathVariable("id") UUID id) {
        log.info("Deleting order with ID: {}", id);
        orderUseCase.deleteOrder(id);
    }

    @Override
    public void cancelOrder(@PathVariable("id") UUID id) {
        log.info("Cancelling order with ID: {}", id);
        orderUseCase.cancelOrder(id);
    }

    @GetMapping("/orders/search")
    public String testBoolean(@RequestParam boolean active) {
        return "Active: " + active;
    }
}