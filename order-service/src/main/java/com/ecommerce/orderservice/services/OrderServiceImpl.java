package com.ecommerce.orderservice.services;

import com.ecommerce.orderservice.configuration.exception.handler.NoSuchElementFoundException;
import com.ecommerce.orderservice.configuration.exception.handler.OrderValidationException;
import com.ecommerce.orderservice.configuration.exception.handler.ProductRetrievalException;
import com.ecommerce.orderservice.feign.ProductFeignClient;
import com.ecommerce.orderservice.model.dto.OrderDto;
import com.ecommerce.orderservice.model.dto.OrderItemDto;
import com.ecommerce.orderservice.model.dto.ProductDto;
import com.ecommerce.orderservice.model.entity.OrderEntity;
import com.ecommerce.orderservice.model.entity.OrderItemEntity;
import com.ecommerce.orderservice.model.entity.OrderStatus;
import com.ecommerce.orderservice.model.request.CreateOrderRequest;
import com.ecommerce.orderservice.model.request.OrderItemRequest;
import com.ecommerce.orderservice.model.request.UpdateOrderRequest;
import com.ecommerce.orderservice.repository.OrderRepository;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
@Service
public record OrderServiceImpl(ProductFeignClient productFeignClient,
                               OrderRepository orderRepository, ModelMapper modelMapper) implements OrderService {

    private static final int ASYNC_VALIDATION_TIMEOUT_SECONDS = 5;

    @Override
    public OrderDto createOrder(CreateOrderRequest request) {
        // Validate order details and product availability
        log.info("Validating order details and product availability for order creation...");
        validateOrderDetails(request);

        // Fetch product details concurrently
        log.info("Fetching product details concurrently...");
        List<OrderItemDto> result = createOrderItemDtosWithProductDetails(request.getItems());

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
        // kafkaTemplate.send("order_created", savedOrder.getId().toString());

        // Initiate asynchronous validation with timeout
        validateOrderAsync(savedOrder);

        // Convert OrderEntity to OrderDto
        var savedOrderDto = modelMapper.map(savedOrder, OrderDto.class);
        savedOrderDto.setItems(result);
        log.info("Order successfully created");
        return savedOrderDto;
    }

    private List<OrderItemDto> createOrderItemDtosWithProductDetails(List<OrderItemRequest> orderItemRequests) {
        Map<Long, ProductDto> productMap = getProductDtosConcurrently(
                orderItemRequests.stream()
                        .map(OrderItemRequest::getProductId)
                        .collect(Collectors.toSet()), getRequestHeaderToken());

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

        final String[] accessToken = {""};
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            accessToken[0] = "Bearer " + jwtAuthenticationToken.getToken().getTokenValue();
        }
        return accessToken;
    }

    private void validateOrderDetails(CreateOrderRequest request) {
        // Check for duplicate items
        Set<Long> uniqueProductIds = new HashSet<>();
        for (OrderItemRequest item : request.getItems()) {
            if (!uniqueProductIds.add(item.getProductId())) {
                throw new OrderValidationException("Duplicate product found with id in the order.");
            }
        }
        // Verify product availability with inventory service
        verifyProductAvailability(request.getItems());

        // Apply order limits based on user or product rules
        applyOrderLimits(request.getUserId(), request.getItems());

    }

    private void verifyProductAvailability(List<OrderItemRequest> items) {
        for (OrderItemRequest item : items) {
            try {
                //  productFeignClient.verifyProductAvailability(item.getProductId(), item.getQuantity());
                log.info("this code soon");
            } catch (FeignException e) {
                throw new OrderValidationException("Product not available: " + e.getMessage());
            }
        }
    }

    private void applyOrderLimits(String userId, List<OrderItemRequest> items) {
        // Example: Apply order limits based on user or product rules (you need to implement this)
        // You may want to check user-specific limits or product-specific limits here.
        // Throw OrderValidationException if any limits are exceeded.
    }

    private void validateOrderAsync(OrderEntity savedOrder) {
        try {
            CompletableFuture.runAsync(() -> {
                // Communicating with payment service for payment authorization (if applicable)
                // paymentService.authorizePayment(order);
                savedOrder.setUpdatedAt(ZonedDateTime.now().toLocalDateTime());
                savedOrder.setStatus(OrderStatus.VALIDATION_SUCCEEDED);
                orderRepository.save(savedOrder);
                // Publishing appropriate events to Kafka
                //kafkaTemplate.send("order_validated", order.getId().toString());

            }).get(ASYNC_VALIDATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);// Timeout for asynchronous validation
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // Handle validation timeout or errors
            log.error("Validation failed or timed out: {}", e.getMessage());
            savedOrder.setUpdatedAt(ZonedDateTime.now().toLocalDateTime());
            savedOrder.setStatus(OrderStatus.VALIDATION_FAILED);
            orderRepository.save(savedOrder);
            // kafkaTemplate.send("validation_failed", savedOrder.getId().toString());
            throw new OrderValidationException("Order validation failed");
        }
    }

    @Override
    public OrderDto getOrderById(Long orderId) {
        log.info("Retrieving order with ID: {}", orderId);

        var orderFound = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementFoundException("Order not found with ID: " + orderId, "P01"));

        // Fetch product details concurrently
        var savedOrderDto = modelMapper.map(orderFound, OrderDto.class);

        List<OrderItemRequest> orderItemRequestList = modelMapper.map(orderFound.getItems(),
                new TypeToken<List<OrderItemRequest>>() {
                }.getType());
        List<OrderItemDto> result = createOrderItemDtosWithProductDetails(orderItemRequestList);

        savedOrderDto.setItems(result);
        log.info("Order retrieved successfully");
        return savedOrderDto;
    }

    @Override
    public OrderDto updateOrder(Long id, UpdateOrderRequest request) {
        return null;
    }

    @Override
    public void cancelOrder(Long id) {

    }

    @Override
    public List<OrderDto> getAllOrders() {
        log.info("Retrieving all orders");

        var orders = orderRepository.findAll();

        // Collect unique product IDs from all orders
        Set<Long> uniqueProductIds = orders.stream()
                .flatMap(order -> order.getItems().stream())
                .map(OrderItemEntity::getProductId)
                .collect(Collectors.toSet());

        // Retrieve product details for unique product IDs concurrently
        var accessToken = getRequestHeaderToken();
        Map<Long, ProductDto> productMap = getProductDtosConcurrently(uniqueProductIds, accessToken);

        log.info("Retrieved {} orders", orders.size());

        // Map orders to OrderDto, filling in product details from the map
        return orders.stream()
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
                .collect(Collectors.toList());
    }

    private Map<Long, ProductDto> getProductDtosConcurrently(Set<Long> productIds, String[] accessToken) {
        var productFutures = productIds.stream()
                .map(productId -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return productFeignClient.getProductById(productId, accessToken[0]);
                    } catch (FeignException e) {
                        log.error("Failed to retrieve product with ID {}: {}", productId, e.getMessage());
                        throw new ProductRetrievalException("Failed to retrieve product with ID " + productId);
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
