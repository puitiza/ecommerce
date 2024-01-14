package com.ecommerce.orderservice.services;

import com.ecommerce.orderservice.clients.ProductFeignClient;
import com.ecommerce.orderservice.model.dto.OrderDto;
import com.ecommerce.orderservice.model.entity.OrderEntity;
import com.ecommerce.orderservice.model.entity.OrderStatus;
import com.ecommerce.orderservice.model.request.CreateOrderRequest;
import com.ecommerce.orderservice.model.request.UpdateOrderRequest;
import com.ecommerce.orderservice.repository.OrderRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public record OrderServiceImpl(ProductFeignClient productFeignClient,
                               OrderRepository orderRepository, ModelMapper modelMapper) implements OrderService {

    @Override
    public OrderDto createOrder(CreateOrderRequest request) {
        // Validate order details and product availability
        /*if (!validateOrder(request)) {
            throw new InvalidOrderException("Invalid order details");
        }*/

        var orderEntity = modelMapper.map(request,OrderEntity.class);

        // Create Order entity
        OrderEntity order = new OrderEntity();
        order.setUserId(request.getUserId());
        order.setItems(orderEntity.getItems());
        order.setShippingAddress(request.getShippingAddress());
        order.setStatus(OrderStatus.VALIDATING);
        order.setCreatedAt(ZonedDateTime.now().toLocalDateTime());

        //Assign Relation Order to OrderItems
        order.getItems().forEach(orderItemEntity -> orderItemEntity.setOrder(order));

        var orderSaved = orderRepository.save(order);
        /*kafkaTemplate.send("order_created", order.getId().toString());

        // Initiate validation process asynchronously
        try {
            CompletableFuture.runAsync(this::validateOrderAsync, order).get(5, TimeUnit.SECONDS); // 5-second timeout
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // Handle validation timeout or errors
            log.error("Validation failed or timed out: {}", e.getMessage());
            order.setStatus(OrderStatus.VALIDATION_FAILED);
            orderRepository.save(order);
            kafkaTemplate.send("validation_failed", order.getId().toString());
            throw new OrderValidationException("Order validation failed");
        }*/
        // Convert Product JPA entity to ProductDto
        return modelMapper.map(orderSaved, OrderDto.class);
    }
    @Override
    public OrderDto getOrderById(Long orderId) {
        return null;
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
        var orders = orderRepository.findAll();

        return orders.stream()
                .map((order) -> modelMapper.map(order, OrderDto.class))
                .collect(Collectors.toList());
    }
}
