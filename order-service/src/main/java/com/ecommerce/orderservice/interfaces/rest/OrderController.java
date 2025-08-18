package com.ecommerce.orderservice.interfaces.rest;

import com.ecommerce.orderservice.application.dto.OrderPageResponse;
import com.ecommerce.orderservice.application.dto.OrderRequest;
import com.ecommerce.orderservice.application.dto.OrderResponse;
import com.ecommerce.orderservice.domain.port.in.OrderUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for handling order-related HTTP requests.
 * This class implements the {@link OrderOpenApi} interface to inherit all
 * Spring and OpenAPI documentation, ensuring a clean and focused implementation.
 * <p>
 * NOTE: Parameter-level annotations (e.g., {@code @Valid}, {@code @RequestBody},
 * {@code @PathVariable}, {@code @RequestParam}) are NOT inherited from the interface.
 * They must be duplicated in the implementing methods to ensure Spring correctly
 * handles request processing, binding, and validation at runtime.
 */
@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController implements OrderOpenApi {

    private final OrderUseCase orderUseCase;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        log.info("Creating order: {}", request);
        return orderUseCase.createOrder(request);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse getById(@PathVariable UUID uuid) {
        log.info("Retrieving order with ID: {}", uuid);
        return orderUseCase.getOrderById(uuid);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse update(@PathVariable UUID uuid, @Valid @RequestBody OrderRequest request) {
        log.info("Updating order with ID: {} with request: {}", uuid, request);
        return orderUseCase.updateOrder(uuid, request);
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{uuid}")
    public void delete(@PathVariable UUID uuid) {
        log.info("Deleting order with ID {}", uuid);
        orderUseCase.deleteOrder(uuid);
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderPageResponse getOrders(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        log.info("Retrieving orders for page {} with size {}", page, size);
        return new OrderPageResponse(orderUseCase.getAllOrders(page, size));
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{orderId}/cancel")
    public void cancelOrder(@PathVariable UUID orderId) {
        log.info("Cancelling order with ID: {}", orderId);
        orderUseCase.cancelOrder(orderId);
    }

    // This method is not part of the main API contract, so it must define its own
    // routing annotations as it does not inherit them from the interface.
    @GetMapping("/orders/search")
    public String testBoolean(@RequestParam boolean active) {
        return "Active: " + active;
    }
}