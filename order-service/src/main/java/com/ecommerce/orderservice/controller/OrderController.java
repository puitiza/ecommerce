package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderWithProducts;
import com.ecommerce.orderservice.services.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/order")
@RestController
public record OrderController(OrderService orderService) {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @GetMapping("/{orderId}")
    public Order getOrderById(@PathVariable Long orderId) {
        logger.info("GETTING ORDER WITH ID {}", orderId);
        return orderService.getOrderById(orderId);
    }

    @GetMapping("/all")
    public List<Order> getAllOrders() {
        logger.info("GETTING ALL ORDERS");
        return orderService.getAllOrders();
    }

    @GetMapping("/withProducts/{orderId}")
    public OrderWithProducts getOrderWithProducts(@PathVariable Long orderId) {
        logger.info("COLLECTING ORDER AND PRODUCT WITH ID {} FROM UPSTREAM SERVICE", orderId);
        return orderService.getOrderWithProducts(orderId);
    }
}
