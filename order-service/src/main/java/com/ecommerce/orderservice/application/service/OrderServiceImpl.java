package com.ecommerce.orderservice.application.service;

import com.ecommerce.orderservice.application.dto.*;
import com.ecommerce.orderservice.application.port.out.PaymentServicePort;
import com.ecommerce.orderservice.application.port.out.ProductServicePort;
import com.ecommerce.orderservice.application.port.out.UserAuthenticationPort;
import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.exception.OrderCancellationException;
import com.ecommerce.orderservice.domain.exception.OrderValidationException;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderItem;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import com.ecommerce.orderservice.domain.port.OrderEventPublisherPort;
import com.ecommerce.orderservice.domain.port.OrderRepositoryPort;
import com.ecommerce.orderservice.domain.service.OrderDomainService;
import com.ecommerce.orderservice.infrastructure.adapter.persistence.mapper.OrderMapper;
import com.ecommerce.shared.exception.ExceptionError;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private final UserAuthenticationPort userAuthenticationPort;
    private final OrderMapper mapper;
    private final StateMachineFactory<OrderStatus, OrderEventType> stateMachineFactory;

    // ExecutorService dedicated for I/O tasks (calls to APIS)
    private final ExecutorService apiThreadPool = Executors.newCachedThreadPool();

    @PreDestroy
    public void shutdown() {
        apiThreadPool.shutdown();
        log.info("API Thread Pool shutdown successfully.");
    }

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
                BigDecimal.ZERO,
                request.shippingAddress()
        );
        order = domainService.calculateTotalPrice(order);

        Order savedOrder = orderRepositoryPort.save(order);
        log.debug("Saved Order with ID: {}", savedOrder.id());

        StateMachine<OrderStatus, OrderEventType> sm = stateMachineFactory.getStateMachine(savedOrder.id().toString());
        sm.startReactively().subscribe();

        Message<OrderEventType> message = MessageBuilder
                .withPayload(OrderEventType.ORDER_CREATED)
                .setHeader("order", savedOrder)
                .build();

        sm.sendEvent(Mono.just(message)).subscribe();
        return mapper.toResponse(savedOrder, itemResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        UserAuthenticationDetails authDetails = userAuthenticationPort.getUserDetails();

        Page<Order> orders = orderRepositoryPort.findAll(page, size);
        Set<Long> productIds = orders.getContent().stream()
                .flatMap(order -> order.items().stream())
                .map(OrderItem::productId)
                .collect(Collectors.toSet());

        Map<Long, ProductResponse> productMap = getProductResponsesConcurrently(productIds, authDetails.token());

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
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id) {
        log.info("Retrieving order with ID: {}", id);
        UserAuthenticationDetails authDetails = userAuthenticationPort.getUserDetails();

        Order order = orderRepositoryPort.findById(id);

        List<OrderItemRequest> itemRequests = order.items().stream()
                .map(item -> new OrderItemRequest(item.productId(), item.quantity()))
                .toList();

        List<OrderItemResponse> items = createOrderItemResponses(itemRequests, authDetails.token());

        log.info("Order retrieved successfully: {}", id);
        return mapper.toResponse(order, items);
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(UUID id, OrderRequest request) {
        log.info("Updating order with ID: {}", id);
        UserAuthenticationDetails authDetails = userAuthenticationPort.getUserDetails();

        Order order = orderRepositoryPort.findById(id);
        validateAndUpdateOrderItems(request.items(), order, authDetails.token());

        if (request.shippingAddress() != null) {
            order = order.withShippingAddress(request.shippingAddress());
        }

        order = domainService.calculateTotalPrice(order);
        order = orderRepositoryPort.save(order);
        eventPublisherPort.publishOrderUpdatedEvent(order);

        List<OrderItemRequest> itemRequests = order.items().stream()
                .map(item -> new OrderItemRequest(item.productId(), item.quantity()))
                .toList();

        List<OrderItemResponse> items = createOrderItemResponses(itemRequests, authDetails.token());

        log.info("Order successfully updated: {}", id);
        return mapper.toResponse(order, items);
    }

    @Override
    @Transactional
    public void cancelOrder(UUID id) {
        log.info("Cancelling order with ID: {}", id);
        UserAuthenticationDetails authDetails = userAuthenticationPort.getUserDetails();

        Order order = orderRepositoryPort.findById(id);

        if (!domainService.canCancel(order)) {
            throw new OrderCancellationException("Order cannot be canceled in its current state");
        }

        initiateCancellationProcesses(order, authDetails.token());
        Order cancelledOrder = order.withStatus(OrderStatus.CANCELLED);
        orderRepositoryPort.save(cancelledOrder);
        eventPublisherPort.publishOrderCancelledEvent(cancelledOrder);
        log.info("Order successfully cancelled: {}", id);
    }

    private List<OrderItemResponse> validateAndGetOrderItemResponses(List<OrderItemRequest> items, String token) {
        // Validate No Duplicates
        Set<Long> uniqueIds = new HashSet<>();
        for (OrderItemRequest item : items) {
            if (!uniqueIds.add(item.productId())) {
                throw new OrderValidationException(ExceptionError.ORDER_DUPLICATE_PRODUCT, item.productId());
            }
        }

        // Fetch batch response
        BatchProductResponse batchResponse = productServicePort.verifyAndGetProducts(new BatchProductRequest(items), token);

        // Check errors
        List<String> errors = batchResponse.products().stream()
                .filter(response -> response.error() != null)
                .map(response -> String.format("Product ID %d: %s", response.productId(), response.error()))
                .toList();

        if (!errors.isEmpty()) {
            throw new OrderValidationException(ExceptionError.ORDER_VALIDATION, String.join("; ", errors));
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
                .map(id -> CompletableFuture.supplyAsync(() -> productServicePort.getProductById(id, token),
                        apiThreadPool))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toMap(ProductResponse::id, p -> p));
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
                currentItems.add(new OrderItem(null, updatedItem.productId(), updatedItem.quantity(), product.price()));
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

        try {
            var futures = order.items().stream()
                    .map(item -> CompletableFuture.runAsync(() -> {
                        ProductResponse product = productServicePort.getProductById(item.productId(), token);
                        int updatedInventory = product.inventory() + item.quantity();
                        productServicePort.updateProductInventory(item.productId(), updatedInventory, token);
                    }, apiThreadPool))
                    .toList();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            throw new OrderCancellationException("Failed to restock items: " + e.getMessage());
        }
    }
}