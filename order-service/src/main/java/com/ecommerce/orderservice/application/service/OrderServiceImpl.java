package com.ecommerce.orderservice.application.service;

import com.ecommerce.orderservice.application.dto.*;
import com.ecommerce.orderservice.application.port.out.OrderEventPublisherPort;
import com.ecommerce.orderservice.application.port.out.PaymentServicePort;
import com.ecommerce.orderservice.application.port.out.ProductServicePort;
import com.ecommerce.orderservice.domain.exception.OrderCancellationException;
import com.ecommerce.orderservice.domain.exception.OrderValidationException;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderItem;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import com.ecommerce.orderservice.domain.port.OrderRepositoryPort;
import com.ecommerce.orderservice.domain.service.OrderDomainService;
import com.ecommerce.orderservice.infrastructure.adapter.persistence.mapper.OrderMapper;
import com.ecommerce.shared.exception.ExceptionError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepositoryPort orderRepositoryPort;
    private final ProductServicePort productServicePort;
    private final PaymentServicePort paymentServicePort;
    private final OrderEventPublisherPort eventPublisherPort;
    private final OrderDomainService domainService;

    private final OrderMapper mapper;
    private static final int ASYNC_VALIDATION_TIMEOUT_SECONDS = 5;

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        var token = getRequestHeaderToken();
        validateOrderDetails(request, token[0]);

        List<OrderItemResponse> items = createOrderItemResponses(request.items(), token[0]);
        Order order = new Order(
                null,
                token[1],
                items.stream()
                        .map(item -> new OrderItem(null, item.productId(), item.quantity(), BigDecimal.valueOf(item.unitPrice())))
                        .collect(Collectors.toSet()),
                OrderStatus.VALIDATING,
                LocalDateTime.now(),
                null,
                BigDecimal.ZERO,
                request.shippingAddress()
        );
        order = domainService.calculateTotalPrice(order);

        Order savedOrder = orderRepositoryPort.save(order);
        log.info("Saved Order with ID: {}", savedOrder.id());
        eventPublisherPort.publishOrderCreatedEvent(savedOrder);

        validateOrderAsync(savedOrder);
        return mapper.toResponse(savedOrder, items);
    }

    @Override
    public Page<OrderResponse> getAllOrders(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        String[] tokenAndUserId = getRequestHeaderToken();
        String token = tokenAndUserId[0];

        Page<Order> orders = orderRepositoryPort.findAll(page, size); // Asume port soporta paginación
        Set<Long> productIds = orders.getContent().stream()
                .flatMap(order -> order.items().stream())
                .map(OrderItem::productId)
                .collect(Collectors.toSet());

        Map<Long, ProductResponse> productMap = getProductResponsesConcurrently(productIds, token);

        List<OrderResponse> responses = orders.getContent().stream()
                .map(order -> {
                    List<OrderItemResponse> items = order.items().stream()
                            .map(item -> {
                                ProductResponse product = productMap.get(item.productId());
                                return new OrderItemResponse(
                                        item.productId(),
                                        product.name(),
                                        item.quantity(),
                                        product.price()
                                );
                            })
                            .toList();
                    return mapper.toResponse(order, items);
                })
                .toList();

        return new PageImpl<>(responses, pageable, orders.getTotalElements());
    }

    @Override
    public OrderResponse getOrderById(UUID id) {
        log.info("Retrieving order with ID: {}", id);
        String[] tokenAndUserId = getRequestHeaderToken();
        String token = tokenAndUserId[0];
        Order order = orderRepositoryPort.findById(id);

        List<OrderItemResponse> items = createOrderItemResponses(
                order.items().stream()
                        .map(item -> new OrderItemRequest(item.productId(), item.quantity()))
                        .toList(),
                token
        );

        log.info("Order retrieved successfully: {}", id);
        return mapper.toResponse(order, items);
    }

    @Override
    public OrderResponse updateOrder(UUID id, OrderRequest request) {
        log.info("Updating order with ID: {}", id);
        String[] tokenAndUserId = getRequestHeaderToken();
        String token = tokenAndUserId[0];

        Order order = orderRepositoryPort.findById(id);
        validateAndUpdateOrderItems(request.items(), order, token);

        if (request.shippingAddress() != null) {
            order = new Order(
                    order.id(),
                    order.userId(),
                    order.items(),
                    order.status(),
                    order.createdAt(),
                    LocalDateTime.now(),
                    order.totalPrice(),
                    request.shippingAddress()
            );
        }

        order = domainService.calculateTotalPrice(order);
        order = orderRepositoryPort.save(order);
        eventPublisherPort.publishOrderUpdatedEvent(order);

        List<OrderItemResponse> items = createOrderItemResponses(
                order.items().stream()
                        .map(item -> new OrderItemRequest(item.productId(), item.quantity()))
                        .toList(),
                token
        );

        log.info("Order successfully updated: {}", id);
        return mapper.toResponse(order, items);
    }

    @Override
    public void cancelOrder(UUID id) {
        log.info("Cancelling order with ID: {}", id);
        String[] tokenAndUserId = getRequestHeaderToken();
        String token = tokenAndUserId[0];

        Order order = orderRepositoryPort.findById(id);

        if (!domainService.canCancel(order)) {
            throw new OrderCancellationException("Order cannot be canceled in its current state");
        }

        initiateCancellationProcesses(order, token);
        Order cancelledOrder = order.withStatus(OrderStatus.CANCELLED);
        orderRepositoryPort.save(cancelledOrder);
        eventPublisherPort.publishOrderCancelledEvent(cancelledOrder);
        log.info("Order successfully cancelled: {}", id);
    }

    private String[] getRequestHeaderToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        final String[] accessToken = {"", ""};
        if (authentication instanceof JwtAuthenticationToken jwt) {
            accessToken[0] = "Bearer " + jwt.getToken().getTokenValue();
            accessToken[1] = jwt.getName();
        }
        return accessToken;
    }

    private void validateOrderDetails(OrderRequest request, String token) {
        Set<Long> uniqueProductIds = new HashSet<>();
        for (OrderItemRequest item : request.items()) {
            if (!uniqueProductIds.add(item.productId())) {
                throw new OrderValidationException(ExceptionError.ORDER_DUPLICATE_PRODUCT, item.productId());
            }
            ProductAvailabilityResponse availability = productServicePort.verifyProductAvailability(item, token);
            if (!availability.isAvailable()) {
                throw new OrderValidationException(ExceptionError.ORDER_INSUFFICIENT_INVENTORY,
                        item.productId(), availability.availableUnits());
            }
        }
    }

    private List<OrderItemResponse> createOrderItemResponses(List<OrderItemRequest> items, String token) {
        Map<Long, ProductResponse> productMap = getProductResponsesConcurrently(
                items.stream().map(OrderItemRequest::productId).collect(Collectors.toSet()),
                token
        );
        return items.stream()
                .map(item -> {
                    ProductResponse product = productMap.get(item.productId());
                    return new OrderItemResponse(
                            item.productId(),
                            product.name(),
                            item.quantity(),
                            product.price()
                    );
                })
                .toList();
    }

    private Map<Long, ProductResponse> getProductResponsesConcurrently(Set<Long> productIds, String token) {
        var futures = productIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> productServicePort.getProductById(id, token)))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toMap(ProductResponse::id, p -> p));
    }

    private void validateOrderAsync(Order order) {
        try {
            CompletableFuture.runAsync(() -> {
                var response = paymentServicePort.authorizePayment(new PaymentAuthorizationRequest(order.id().toString(), order.totalPrice()));
                if (response.authorized()) {
                    Order validatedOrder = order.withStatus(OrderStatus.VALIDATION_SUCCEEDED);
                    //validatedOrder.updatedAt(LocalDateTime.now());
                    orderRepositoryPort.save(validatedOrder);
                    eventPublisherPort.publishOrderValidatedEvent(validatedOrder);
                } else {
                    throw new OrderValidationException("Payment Authorization Denied");
                }
            }).get(ASYNC_VALIDATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.info("Handling validation failure for order ID: {}", order.id().toString());
            Order failedOrder = order.withStatus(OrderStatus.VALIDATION_FAILED);
            orderRepositoryPort.save(failedOrder);
            eventPublisherPort.publishValidationFailedEvent(failedOrder);
            throw new OrderValidationException(e.getMessage());
        }
    }

    private void validateAndUpdateOrderItems(List<OrderItemRequest> updatedItems, Order order, String token) {
        Set<OrderItem> currentItems = new HashSet<>(order.items());
        currentItems.removeIf(item -> updatedItems.stream()
                .noneMatch(updated -> updated.productId().equals(item.productId())));

        for (OrderItemRequest updatedItem : updatedItems) {
            ProductAvailabilityResponse availability = productServicePort.verifyProductAvailability(updatedItem, token);
            if (!availability.isAvailable()) {
                throw new OrderValidationException(ExceptionError.ORDER_INSUFFICIENT_INVENTORY,
                        updatedItem.productId(), availability.availableUnits());
            }

            Optional<OrderItem> existingItem = currentItems.stream()
                    .filter(item -> item.productId().equals(updatedItem.productId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                OrderItem item = existingItem.get();
                currentItems.remove(item);
                currentItems.add(new OrderItem(item.id(), item.productId(), updatedItem.quantity(), item.unitPrice()));
            } else {
                ProductResponse product = productServicePort.getProductById(updatedItem.productId(), token);
                currentItems.add(new OrderItem(null, updatedItem.productId(), updatedItem.quantity(), BigDecimal.valueOf(product.price())));
            }
        }
    }

    private void initiateCancellationProcesses(Order order, String token) {
        try {
            String paymentId = paymentServicePort.findPaymentIdByOrderId(order.id().toString());
            paymentServicePort.initiateRefund(paymentId, new RefundRequest(order.totalPrice()));
        } catch (Exception e) {
            throw new OrderCancellationException("Failed to initiate refund for order: " + e.getMessage(), e);
        }

        ExecutorService restockThreadPool = Executors.newFixedThreadPool(5);
        try {
            var futures = order.items().stream()
                    .map(item -> CompletableFuture.runAsync(() -> {
                        ProductResponse product = productServicePort.getProductById(item.productId(), token);
                        int updatedInventory = product.inventory() + item.quantity();
                        productServicePort.updateProductInventory(item.productId(), updatedInventory, token);
                    }, restockThreadPool))
                    .toList();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            throw new OrderCancellationException("Failed to restock items: " + e.getMessage());
        } finally {
            restockThreadPool.shutdown();
        }
    }

}