package com.ecommerce.orderservice.services;

import com.ecommerce.orderservice.configuration.exception.handler.NoSuchElementFoundException;
import com.ecommerce.orderservice.configuration.exception.handler.OrderCancellationException;
import com.ecommerce.orderservice.configuration.exception.handler.OrderValidationException;
import com.ecommerce.orderservice.configuration.exception.handler.ProductRetrievalException;
import com.ecommerce.orderservice.feign.PaymentFeignClient;
import com.ecommerce.orderservice.feign.ProductFeignClient;
import com.ecommerce.orderservice.model.dto.OrderDto;
import com.ecommerce.orderservice.model.dto.OrderItemDto;
import com.ecommerce.orderservice.model.dto.ProductDto;
import com.ecommerce.orderservice.model.entity.OrderEntity;
import com.ecommerce.orderservice.model.entity.OrderItemEntity;
import com.ecommerce.orderservice.model.entity.OrderStatus;
import com.ecommerce.orderservice.model.request.*;
import com.ecommerce.orderservice.publisher.OrderEventPublisher;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.services.component.OrderStateMachine;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public record OrderServiceImpl(
        ProductFeignClient productFeignClient, PaymentFeignClient paymentFeignClient,
        OrderEventPublisher orderEventPublisher, OrderStateMachine orderStateMachine,
        OrderRepository orderRepository, ModelMapper modelMapper) implements OrderService {

    private static final int ASYNC_VALIDATION_TIMEOUT_SECONDS = 5;

    @Override
    public OrderDto createOrder(CreateOrderRequest request) {
        var token = getRequestHeaderToken(); // Get token once

        // Validate order details and product availability
        log.info("Validating order details and product availability for order creation...");
        validateOrderDetails(request, token);

        // Fetch product details concurrently
        log.info("Fetching product details concurrently...");
        List<OrderItemDto> result = createOrderItemDtosWithProductDetails(request.getItems(), token[0]);

        var orderEntity = modelMapper.map(request, OrderEntity.class);

        // Create Order entity
        log.info("Creating Order entity...");
        OrderEntity order = new OrderEntity();
        order.setUserId(request.getUserId());
        order.setItems(orderEntity.getItems());
        order.setShippingAddress(request.getShippingAddress());
        order.setStatus(OrderStatus.VALIDATING);
        order.setCreatedAt(ZonedDateTime.now().toLocalDateTime());

        //Assign Relation Order to OrderItems
        order.getItems().forEach(orderItemEntity -> orderItemEntity.setOrder(order));

        // Calculate total price
        BigDecimal totalPrice = result.stream()
                .map(item -> BigDecimal.valueOf(item.getUnitPrice()).multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(totalPrice);

        var savedOrder = orderRepository.save(order);

        // Send order_created event to Kafka
        orderEventPublisher.publishOrderCreatedEvent(savedOrder);

        // Initiate asynchronous validation with timeout
        validateOrderAsync(savedOrder);

        // Convert OrderEntity to OrderDto
        var savedOrderDto = modelMapper.map(savedOrder, OrderDto.class);
        savedOrderDto.setItems(result);
        log.info("Order successfully created");
        return savedOrderDto;
    }

    private List<OrderItemDto> createOrderItemDtosWithProductDetails(List<OrderItemRequest> orderItemRequests, String accessToken) {
        Map<Long, ProductDto> productMap = getProductDtosConcurrently(
                orderItemRequests.stream()
                        .map(OrderItemRequest::getProductId)
                        .collect(Collectors.toSet()), accessToken);

        // Create OrderItemDtos with product details
        return orderItemRequests.stream()
                .map(item -> {
                    var product = productMap.get(item.getProductId());
                    return new OrderItemDto(product.getId(), product.getName(), item.getQuantity(), product.getPrice());
                })
                .toList();
    }

    private String[] getRequestHeaderToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authentication found: {}", authentication);

        final String[] accessToken = {"", ""};
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            accessToken[0] = "Bearer " + jwtAuthenticationToken.getToken().getTokenValue();
        }
        accessToken[1] = authentication.getName();
        return accessToken;
    }

    private void validateOrderDetails(CreateOrderRequest request, String[] token) {
        // Check for duplicate items
        Set<Long> uniqueProductIds = new HashSet<>();
        for (OrderItemRequest item : request.getItems()) {
            if (!uniqueProductIds.add(item.getProductId())) {
                throw new OrderValidationException(
                        String.format("Duplicate product found with id: %d in the order", item.getProductId()));
            }
        }
        // Verify product availability
        for (OrderItemRequest item : request.getItems()) {
            try {
                var availabilityResponse = productFeignClient.verifyProductAvailability(item, token[0]);
                if (!availabilityResponse.isAvailable()) {
                    throw new OrderValidationException(
                            String.format("Insufficient inventory for productId %d. Only %d units available.",
                                    item.getProductId(), availabilityResponse.getAvailableUnits()));
                }
            } catch (FeignException e) {
                throw new OrderValidationException(
                        String.format("Failed to check product availability for productId %d due to communication error: %s", item.getProductId(), e.getMessage()));
            }
        }

        // Extract user ID from JWT claims
        request.extractUserIdFromToken(token[1]);
    }

    private void validateOrderAsync(OrderEntity savedOrder) {
        try {
            CompletableFuture.runAsync(() -> {
                try {
                    // Communicating with payment service for payment authorization (if applicable)
                    boolean paymentAuthorized = paymentFeignClient.authorizePayment(
                            new PaymentAuthorizationRequest(savedOrder.getId(), savedOrder.getTotalPrice()));

                    if (paymentAuthorized) {
                        savedOrder.setUpdatedAt(ZonedDateTime.now().toLocalDateTime());
                        savedOrder.setStatus(OrderStatus.VALIDATION_SUCCEEDED);
                        orderRepository.save(savedOrder);

                        // Publishing appropriate events to Kafka
                        orderEventPublisher.publishOrderValidatedEvent(savedOrder);
                    } else {
                        handleValidationFailure(savedOrder, "Payment authorization denied", null);
                    }
                } catch (FeignException e) {
                    handleValidationFailure(savedOrder, "Failed to authorize payment", e);
                }

            }).get(ASYNC_VALIDATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);// Timeout for asynchronous validation
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            handleValidationFailure(savedOrder, "Validation failed or timed out", e);
        }
    }

    private void handleValidationFailure(OrderEntity savedOrder, String errorMessage, Throwable exception) {
        log.error("{}: {}", errorMessage, exception == null ? "" : exception.getMessage());
        savedOrder.setUpdatedAt(ZonedDateTime.now().toLocalDateTime());
        savedOrder.setStatus(OrderStatus.VALIDATION_FAILED);
        orderRepository.save(savedOrder);
        orderEventPublisher.publishValidationFailedEvent(savedOrder);
        throw new OrderValidationException("Order validation failed");
    }

    @Override
    public OrderDto getOrderById(Long orderId) {
        log.info("Retrieving order with ID: {}", orderId);

        var orderFound = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementFoundException(String.format("Order with ID %d not found", orderId), "P01"));

        var token = getRequestHeaderToken(); // Get token once

        // Fetch product details concurrently
        var savedOrderDto = modelMapper.map(orderFound, OrderDto.class);

        List<OrderItemRequest> orderItemRequestList = modelMapper.map(orderFound.getItems(),
                new TypeToken<List<OrderItemRequest>>() {
                }.getType());
        List<OrderItemDto> result = createOrderItemDtosWithProductDetails(orderItemRequestList, token[0]);

        savedOrderDto.setItems(result);
        log.info("Order retrieved successfully");
        return savedOrderDto;
    }

    @Override
    public OrderDto updateOrder(Long id, UpdateOrderRequest request) {
        log.info("Updating order with ID: {}", id);

        OrderEntity orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementFoundException(String.format("Order with ID %d not found", id), "P01"));

        var token = getRequestHeaderToken(); // Get token once

        // Validate and update items and quantities
        validateAndUpdateOrderItems(request.getItems(), orderEntity, token[0]);

        // Update shipping address if provided
        if (request.getShippingAddress() != null) {
            orderEntity.setShippingAddress(request.getShippingAddress());
        }
        // Fetch product details concurrently
        List<OrderItemRequest> orderItemRequestList = modelMapper.map(orderEntity.getItems(),
                new TypeToken<List<OrderItemRequest>>() {
                }.getType());
        List<OrderItemDto> result = createOrderItemDtosWithProductDetails(orderItemRequestList, token[0]);

        // Calculate updated total price
        BigDecimal totalPrice = result.stream()
                .map(item -> BigDecimal.valueOf(item.getUnitPrice()).multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orderEntity.setTotalPrice(totalPrice);

        // Save the updated order entity
        orderEntity.setUpdatedAt(ZonedDateTime.now().toLocalDateTime());
        OrderEntity updatedOrder = orderRepository.save(orderEntity);

        // Publish order_updated event to Kafka
        orderEventPublisher.publishOrderUpdatedEvent(updatedOrder);

        // Convert OrderEntity to OrderDto
        OrderDto updatedOrderDto = modelMapper.map(updatedOrder, OrderDto.class);
        updatedOrderDto.setItems(result);

        log.info("Order successfully updated");
        return updatedOrderDto;
    }

    private void validateAndUpdateOrderItems(List<OrderItemRequest> updatedItems, OrderEntity orderEntity, String accessToken) {
        // Remove existing items not present in the updated request
        orderEntity.getItems()
                .removeIf(item -> updatedItems.stream()
                        .noneMatch(updatedItem -> item.getProductId().equals(updatedItem.getProductId())));

        // Add or update quantities for existing items
        for (OrderItemRequest updatedItem : updatedItems) {
            OrderItemEntity existingItem = orderEntity.getItems().stream()
                    .filter(item -> item.getProductId().equals(updatedItem.getProductId()))
                    .findFirst()
                    .orElse(null);

            if (existingItem != null) {
                // Update existing item quantity
                existingItem.setQuantity(updatedItem.getQuantity());
            } else {
                // Add new item
                OrderItemEntity newItem = modelMapper.map(updatedItem, OrderItemEntity.class);
                newItem.setOrder(orderEntity);
                orderEntity.getItems().add(newItem);
            }

            // Validate quantity (e.g., check against product availability)
            validateUpdatedQuantity(updatedItem, accessToken);
        }
    }

    private void validateUpdatedQuantity(OrderItemRequest updatedItem, String token) {
        try {
            var availabilityResponse = productFeignClient.verifyProductAvailability(updatedItem, token);
            if (!availabilityResponse.isAvailable()) {
                throw new OrderValidationException(
                        String.format("Insufficient inventory for productId %d. Only %d units available.",
                                updatedItem.getProductId(), availabilityResponse.getAvailableUnits()));
            }
        } catch (FeignException e) {
            throw new OrderValidationException(
                    String.format("Failed to check product availability for productId %d due to communication error: %s", updatedItem.getProductId(), e.getMessage()));
        }
    }

    @Override
    public void cancelOrder(Long id) {
        log.info("Cancelling order with ID: {}", id);

        OrderEntity orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementFoundException(String.format("Order with ID %d not found", id), "P01"));

        var token = getRequestHeaderToken(); // Get token once

        // Check if cancellation is allowed based on order status and business rules
        if (!orderStateMachine.canCancel(orderEntity)) {
            throw new OrderCancellationException("Order cannot be canceled in its current state");
        }
        // Initiate any necessary reversal processes (e.g., payment refunds, inventory restocking)
        initiateCancellationProcesses(orderEntity, token[0]);

        // Save the updated order entity
        orderEntity.setStatus(OrderStatus.CANCELLED);
        orderEntity.setUpdatedAt(ZonedDateTime.now().toLocalDateTime());
        orderRepository.save(orderEntity);

        // Publish order_cancelled event to Kafka
        orderEventPublisher.publishOrderCancelledEvent(orderEntity);

        log.info("Order successfully cancelled");
    }

    private void initiateCancellationProcesses(OrderEntity orderEntity, String accessToken) {
        // Initiate payment refund
        try {
            String paymentId = paymentFeignClient.findPaymentIdByOrderId(String.valueOf(orderEntity.getId()));
            BigDecimal refundAmount = orderEntity.getTotalPrice();
            RefundRequest refundRequest = new RefundRequest(orderEntity.getId(), refundAmount);
            paymentFeignClient.initiateRefund(paymentId, refundRequest);
        } catch (FeignException e) {
            // Handle refund failure (e.g., log, notify admin)
            log.error("Failed to initiate refund for order {}: {}", orderEntity.getId(), e.getMessage());
            throw new OrderCancellationException("Failed to initiate refund for the order");
        }

        // Restock items in product-service inventory
        ExecutorService restockThreadPool = Executors.newFixedThreadPool(5); // Adjust thread pool size as needed
        try {
            var productFutures = orderEntity.getItems().stream()
                    .map(item -> CompletableFuture.runAsync(() -> {
                        ProductDto productDto = productFeignClient.getProductById(item.getProductId(), accessToken);
                        int updatedInventory = productDto.getInventory() - item.getQuantity();
                        productFeignClient.updateProductInventory(productDto.getId(), updatedInventory, accessToken);
                    }, restockThreadPool))
                    .toList();
            CompletableFuture.allOf(productFutures.toArray(new CompletableFuture[0])).join();
        } catch (FeignException e) {
            // Handle restocking failure (e.g., log, notify admin)
            log.error("Failed to restock items for order {}: {}", orderEntity.getId(), e.getMessage());
            throw new OrderCancellationException("Failed to restock items for the order");
        } finally {
            restockThreadPool.shutdown(); // Ensure the thread pool is shut down
        }
    }

    @Override
    public Page<OrderDto> getAllOrders(int page, int size) {
        log.info("Retrieving all orders");

        Pageable pageable = PageRequest.of(page, size);
        var orders = orderRepository.findAll(pageable);

        // Collect unique product IDs from all orders
        Set<Long> uniqueProductIds = orders.stream()
                .flatMap(order -> order.getItems().stream())
                .map(OrderItemEntity::getProductId)
                .collect(Collectors.toSet());

        // Retrieve product details for unique product IDs concurrently
        var accessToken = getRequestHeaderToken();
        Map<Long, ProductDto> productMap = getProductDtosConcurrently(uniqueProductIds, accessToken[0]);

        log.info("Retrieved {} orders", orders.getNumberOfElements());

        // Map orders to OrderDto, filling in product details from the map
        List<OrderDto> orderDtos = orders.stream()
                .map(order -> {
                    List<OrderItemDto> itemDtos = order.getItems().stream()
                            .map(item -> {
                                ProductDto productDto = productMap.get(item.getProductId());
                                return new OrderItemDto(productDto.getId(), productDto.getName(), item.getQuantity(), productDto.getPrice());
                            })
                            .collect(Collectors.toList());
                    OrderDto orderDto = modelMapper.map(order, OrderDto.class);
                    orderDto.setItems(itemDtos);
                    return orderDto;
                })
                .toList();
        return new PageImpl<>(orderDtos, pageable, orders.getTotalElements());

    }

    private Map<Long, ProductDto> getProductDtosConcurrently(Set<Long> productIds, String accessToken) {
        var productFutures = productIds.stream()
                .map(productId -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return productFeignClient.getProductById(productId, accessToken);
                    } catch (FeignException e) {
                        log.error("Failed to retrieve product with ID {}: {}", productId, e.getMessage());
                        throw new ProductRetrievalException(String.format("Failed to retrieve product with ID %d from the Product Service", productId));
                    }
                }))
                .toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(productFutures.toArray(new CompletableFuture[0]));
        return allFutures.thenApply(v -> productFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toMap(ProductDto::getId, productDto -> productDto)))
                .join();
    }
}
