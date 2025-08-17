package com.ecommerce.orderservice.application.service;

import com.ecommerce.orderservice.application.dto.*;
import com.ecommerce.orderservice.domain.exception.OrderCancellationException;
import com.ecommerce.orderservice.domain.exception.OrderUpdateException;
import com.ecommerce.orderservice.domain.exception.OrderValidationException;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderItem;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import com.ecommerce.orderservice.domain.port.out.OrderRepositoryPort;
import com.ecommerce.orderservice.domain.port.out.ProductServicePort;
import com.ecommerce.orderservice.domain.port.out.UserAuthenticationPort;
import com.ecommerce.orderservice.domain.service.OrderDomainService;
import com.ecommerce.orderservice.infrastructure.adapter.persistence.mapper.OrderMapper;
import com.ecommerce.orderservice.infrastructure.adapter.security.dto.UserAuthenticationDetails;
import com.ecommerce.shared.exception.ExceptionError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements the OrderUseCase interface, providing the core application logic for order management.
 * This service orchestrates calls to domain services and infrastructure ports.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderApplicationServiceImpl implements OrderApplicationService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ProductServicePort productServicePort;
    private final OrderDomainService domainService;
    private final UserAuthenticationPort userAuthenticationPort;
    private final OrderMapper mapper;

    /**
     * Creates a new order by validating the requested items,
     * saving the order to the database, and initiating the
     * state machine process.
     *
     * @param request The order details provided by the client.
     * @return The response object of the created order.
     */
    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        UserAuthenticationDetails authDetails = userAuthenticationPort.getUserDetails();

        // Validate and fetch product details/availability
        List<OrderItemResponse> itemResponses = validateAndGetOrderItemResponses(request.items(), authDetails.token());

        Order order = new Order(
                null,
                authDetails.userId(),
                itemResponses.stream()
                        .map(item -> new OrderItem(
                                null,
                                item.productId(),
                                item.quantity(),
                                item.unitPrice()
                        ))
                        .collect(Collectors.toSet()),
                OrderStatus.CREATED,
                LocalDateTime.now(),
                null,
                calculateTotalPrice(itemResponses),
                request.shippingAddress()
        );

        Order savedOrder = orderRepositoryPort.save(order);
        log.debug("Saved Order with ID: {}", savedOrder.id());

        //Trigger the state machine to start the validation saga.
        domainService.sendCreateEvent(savedOrder);

        return mapper.toResponse(savedOrder, itemResponses);
    }

    /**
     * Retrieves all orders for the authenticated user with pagination.
     *
     * @param page The page number to retrieve (0-indexed).
     * @param size The number of orders per page.
     * @return A paginated list of order responses.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        UserAuthenticationDetails authDetails = userAuthenticationPort.getUserDetails();

        Page<Order> ordersPage = orderRepositoryPort.findAll(page, size);

        List<OrderResponse> responses = ordersPage.getContent().stream()
                .map(order -> enrichOrderWithProductDetails(order, authDetails.token()))
                .toList();
        return new PageImpl<>(responses, pageable, ordersPage.getTotalElements());
    }

    /**
     * Retrieves a single order by its unique identifier.
     *
     * @param id The UUID of the order.
     * @return The order response.
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id) {
        log.debug("Retrieving order with ID: {}", id);
        UserAuthenticationDetails authDetails = userAuthenticationPort.getUserDetails();

        Order order = orderRepositoryPort.findById(id);
        OrderResponse response = enrichOrderWithProductDetails(order, authDetails.token());

        log.info("Order retrieved successfully: {}", id);
        return response;
    }

    /**
     * Updates an existing order. This is only allowed for orders in the CREATED state.
     * The update restarts the entire validation saga with the new order details.
     *
     * @param id      The UUID of the order to update.
     * @param request The updated order details.
     * @return The response object of the updated order.
     */
    @Override
    @Transactional
    public OrderResponse updateOrder(UUID id, OrderRequest request) {
        log.debug("Updating order with ID: {}", id);
        UserAuthenticationDetails authDetails = userAuthenticationPort.getUserDetails();
        Order order = orderRepositoryPort.findById(id);

        if (!domainService.canUpdate(order)) {
            throw new OrderUpdateException(order.status().toString());
        }

        List<OrderItemResponse> itemResponses = validateAndGetOrderItemResponses(request.items(), authDetails.token());
        Set<OrderItem> newItems = itemResponses.stream()
                .map(item -> new OrderItem(null, item.productId(), item.quantity(), item.unitPrice()))
                .collect(Collectors.toSet());
        BigDecimal newTotal = calculateTotalPrice(itemResponses);

        Order updatedOrder = new Order(
                order.id(),
                order.userId(),
                newItems,
                order.status(),
                order.createdAt(),
                LocalDateTime.now(),
                newTotal,
                request.shippingAddress() != null ? request.shippingAddress() : order.shippingAddress()
        );

        Order savedOrder = orderRepositoryPort.save(updatedOrder);

        // Restart the flow of the state machine with the Order_CREATED event
        domainService.sendCreateEvent(savedOrder);

        log.debug("Order successfully updated: {}", id);
        return mapper.toResponse(savedOrder, itemResponses);
    }

    /**
     * Deletes an order (not implemented).
     *
     * @param id The UUID of the order to delete.
     */
    @Override
    public void deleteOrder(UUID id) {
        // Not implemented. Orders are typically not deleted, only cancelled or fulfilled.
    }

    /**
     * Cancels an order. The order must be in a cancellable state as defined
     * by the domain rules. This triggers a saga for compensation (e.g., refunds).
     *
     * @param id The UUID of the order to cancel.
     */
    @Override
    @Transactional
    public void cancelOrder(UUID id) {
        log.debug("Cancelling order with ID: {}", id);

        Order order = orderRepositoryPort.findById(id);
        if (!domainService.canCancel(order)) {
            throw new OrderCancellationException("Order cannot be canceled in its current state");
        }

        domainService.sendCancelEvent(order);

        log.info("Cancel order sent to state machine for order ID: {}", id);
    }

    /**
     * Enriches a single Order domain model with product details from the product-service.
     * This method handles the batch call and error management for consistency.
     *
     * @param order The order to enrich.
     * @param token The user's authentication token.
     * @return An enriched OrderResponse.
     */
    private OrderResponse enrichOrderWithProductDetails(Order order, String token) {
        List<Long> productIds = order.items().stream()
                .map(OrderItem::productId)
                .toList();

        var batchResponses = productServicePort.getProductsDetailsInBatch(new BatchProductDetailsRequest(productIds), token);

        Map<Long, BatchProductDetailsResponse> productDetailsMap = batchResponses.stream()
                .collect(Collectors.toMap(
                        response -> response.product() != null ? response.product().id() : 0L,
                        response -> response
                ));

        List<OrderItemResponse> itemResponses = order.items().stream()
                .map(item -> {
                    BatchProductDetailsResponse batchResponse = productDetailsMap.get(item.productId());

                    if (batchResponse == null || batchResponse.product() == null) {
                        log.warn("Product details not found for ID: {}. Returning fallback item.", item.productId());
                        return new OrderItemResponse(
                                item.productId(),
                                "Product not found",
                                item.quantity(),
                                item.unitPrice()
                        );
                    }

                    ProductResponse productDetails = batchResponse.product();
                    return new OrderItemResponse(
                            productDetails.id(),
                            productDetails.name(),
                            item.quantity(),
                            productDetails.price()
                    );
                })
                .toList();

        return mapper.toResponse(order, itemResponses);
    }

    private List<OrderItemResponse> validateAndGetOrderItemResponses(List<OrderItemRequest> items, String token) {
        // Validate No Duplicates
        Set<Long> uniqueIds = new HashSet<>();
        for (OrderItemRequest item : items) {
            if (!uniqueIds.add(item.productId())) {
                throw new OrderValidationException(ExceptionError.ORDER_DUPLICATE_PRODUCT, item.productId());
            }
        }

        BatchProductResponse batchResponse = productServicePort.verifyAndGetProducts(new BatchProductRequest(items), token);

        // Check errors
        List<String> errors = batchResponse.products().stream()
                .filter(response -> response.error() != null)
                .map(response -> String.format("Product ID %d: %s", response.productId(), response.error()))
                .toList();

        if (!errors.isEmpty()) {
            throw new OrderValidationException(String.join("; ", errors));
        }

        return batchResponse.products().stream()
                .map(response -> new OrderItemResponse(
                        response.productId(),
                        response.name(),
                        items.stream()
                                .filter(item -> item.productId().equals(response.productId()))
                                .findFirst()
                                .orElseThrow()
                                .quantity(),
                        response.price()
                ))
                .toList();
    }

    /**
     * Calculates the total price of all items in a list.
     *
     * @param items The list of order items.
     * @return The total price.
     */
    private BigDecimal calculateTotalPrice(List<OrderItemResponse> items) {
        return items.stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

}