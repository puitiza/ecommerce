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
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {
        log.info("Creating order: {}", request);
        return orderUseCase.createOrder(request);
    }

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderPageResponse getOrders(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        log.info("Retrieving orders for page {} with size {}", page, size);
        return new OrderPageResponse(orderUseCase.getAllOrders(page, size));
    }

    @Override
    @GetMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse getOrderById(@PathVariable UUID orderId) {
        log.info("Retrieving order with ID: {}", orderId);
        return orderUseCase.getOrderById(orderId);
    }

    @Override
    @PutMapping(value = "/{orderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse updateOrder(@PathVariable UUID orderId, @Valid @RequestBody OrderRequest request) {
        log.info("Updating order with ID: {} with request: {}", orderId, request);
        return orderUseCase.updateOrder(orderId, request);
    }

    @Override
    @DeleteMapping(value = "/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable UUID orderId) {
        log.info("DELETING ORDER WITH ID {}", orderId);
        orderUseCase.deleteOrder(orderId);
    }

    @Override
    @PostMapping(value = "/{orderId}/cancel")
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