package com.ecommerce.orderservice.application.service;

import com.ecommerce.orderservice.application.dto.OrderItemResponse;
import com.ecommerce.orderservice.application.dto.OrderResponse;
import com.ecommerce.orderservice.application.dto.ProductAvailabilityResponse;
import com.ecommerce.orderservice.application.dto.ProductResponse;
import com.ecommerce.orderservice.application.port.out.OrderEventPublisherPort;
import com.ecommerce.orderservice.application.port.out.PaymentServicePort;
import com.ecommerce.orderservice.application.port.out.ProductServicePort;
import com.ecommerce.orderservice.application.request.*;
import com.ecommerce.orderservice.domain.exception.OrderCancellationException;
import com.ecommerce.orderservice.domain.exception.OrderValidationException;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderItem;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import com.ecommerce.orderservice.domain.port.OrderRepositoryPort;
import com.ecommerce.orderservice.domain.service.OrderDomainService;
import com.ecommerce.orderservice.infrastructure.adapter.persistence.entity.OrderEntity;
import com.ecommerce.orderservice.infrastructure.adapter.persistence.entity.OrderItemEntity;
import com.ecommerce.orderservice.infrastructure.adapter.persistence.mapper.OrderMapper;
import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    public OrderResponse createOrder(CreateOrderRequest request) {
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
        Page<Order> orders = orderRepositoryPort.findAll(page,size);
        var token = getRequestHeaderToken();

        Set<Long> productIds = entities.stream()
                .flatMap(order -> order.getItems().stream())
                .map(OrderItemEntity::getProductId)
                .collect(Collectors.toSet());
        Map<Long, ProductResponse> productMap = getProductResponsesConcurrently(productIds, token[0]);

        List<OrderResponse> responses = entities.stream()
                .map(entity -> {
                    List<OrderItemResponse> items = entity.getItems().stream()
                            .map(item -> {
                                ProductResponse product = productMap.get(item.getProductId());
                                return new OrderItemResponse(
                                        item.getProductId(),
                                        product.name(),
                                        item.getQuantity(),
                                        product.price()
                                );
                            })
                            .toList();
                    return mapper.toResponse(mapper.toDomain(entity), items);
                })
                .toList();
        return new PageImpl<>(responses, pageable, entities.getTotalElements());
    }

    @Override
    public OrderResponse getOrderById(UUID id) {
        log.info("Retrieving order with ID: {}", id);
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id.toString()));
        var token = getRequestHeaderToken();
        List<OrderItemResponse> items = createOrderItemResponses(
                entity.getItems().stream()
                        .map(item -> new OrderItemRequest(item.getProductId(), item.getQuantity()))
                        .toList(),
                token[0]
        );
        log.info("Order retrieved successfully: {}", id);
        return mapper.toResponse(mapper.toDomain(entity), items);
    }

    @Override
    public OrderResponse updateOrder(UUID id, UpdateOrderRequest request) {
        log.info("Updating order with ID: {}", id);
        String[] tokenAndUserId = getRequestHeaderToken();
        String token = tokenAndUserId[0];

        Order order = orderRepositoryPort.findById(id);
        validateAndUpdateOrderItems(request.items(), order, token);

        if (request.shippingAddress() != null) {
            entity.setShippingAddress(request.shippingAddress());
        }

        List<OrderItemResponse> items = createOrderItemResponses(
                entity.getItems().stream()
                        .map(item -> new OrderItemRequest(item.getProductId(), item.getQuantity()))
                        .toList(),
                token[0]
        );
        Order order = mapper.toDomain(entity);
        order = domainService.calculateTotalPrice(order);
        entity = mapper.toEntity(order);
        entity.setUpdatedAt(LocalDateTime.now());
        entity = orderRepository.save(entity);
        eventPublisherPort.publishOrderUpdatedEvent(mapper.toDomain(entity));
        log.info("Order successfully updated: {}", id);
        return mapper.toResponse(mapper.toDomain(entity), items);
    }

    @Override
    public void cancelOrder(UUID id) {
        log.info("Cancelling order with ID: {}", id);
        OrderEntity entity = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id.toString()));
        Order order = mapper.toDomain(entity);
        var token = getRequestHeaderToken();

        if (!domainService.canCancel(order)) {
            throw new OrderCancellationException("Order cannot be canceled in its current state");
        }

        initiateCancellationProcesses(entity, token[0]);
        entity.setStatus(OrderStatus.CANCELLED);
        entity.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(entity);
        eventPublisherPort.publishOrderCancelledEvent(mapper.toDomain(entity));
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

    private void validateOrderDetails(CreateOrderRequest request, String token) {
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

    private void validateAndUpdateOrderItems(List<OrderItemRequest> updatedItems, OrderEntity entity, String token) {
        entity.getItems().removeIf(item -> updatedItems.stream()
                .noneMatch(updated -> updated.productId().equals(item.getProductId())));

        for (OrderItemRequest updatedItem : updatedItems) {
            ProductAvailabilityResponse availability = productServicePort.verifyProductAvailability(updatedItem, token);
            if (!availability.isAvailable()) {
                throw new OrderValidationException(ExceptionError.ORDER_INSUFFICIENT_INVENTORY,
                        updatedItem.productId(), availability.availableUnits());
            }

            OrderItemEntity existingItem = entity.getItems().stream()
                    .filter(item -> item.getProductId().equals(updatedItem.productId()))
                    .findFirst()
                    .orElse(null);

            if (existingItem != null) {
                existingItem.setQuantity(updatedItem.quantity());
            } else {
                OrderItemEntity newItem = new OrderItemEntity();
                newItem.setProductId(updatedItem.productId());
                newItem.setQuantity(updatedItem.quantity());
                newItem.setOrder(entity);
                ProductResponse product = productServicePort.getProductById(updatedItem.productId(), token);
                newItem.setUnitPrice(BigDecimal.valueOf(product.price()));
                entity.getItems().add(newItem);
            }
        }
    }

    private void initiateCancellationProcesses(OrderEntity entity, String token) {
        try {
            String paymentId = paymentServicePort.findPaymentIdByOrderId(entity.getId().toString());
            paymentServicePort.initiateRefund(paymentId, new RefundRequest(entity.getTotalPrice()));
        } catch (Exception e) {
            throw new OrderCancellationException("Failed to initiate refund for order: " + e.getMessage(), e);
        }

        ExecutorService restockThreadPool = Executors.newFixedThreadPool(5);
        try {
            var futures = entity.getItems().stream()
                    .map(item -> CompletableFuture.runAsync(() -> {
                        ProductResponse product = productServicePort.getProductById(item.getProductId(), token);
                        int updatedInventory = product.inventory() + item.getQuantity();
                        productServicePort.updateProductInventory(item.getProductId(), updatedInventory, token);
                    }, restockThreadPool))
                    .toList();
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            throw new OrderCancellationException("Failed to restock items for order: " + e.getMessage(), e);
        } finally {
            restockThreadPool.shutdown();
        }
    }

}