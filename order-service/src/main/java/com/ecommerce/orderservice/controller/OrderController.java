package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.model.OrderResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@RestController
public class OrderController {
    @GetMapping("/order")
    public List<OrderResponse> getOrders(){

        return Arrays.asList(
                new OrderResponse(1, "Spring ", new BigDecimal(0)),
                new OrderResponse(2, "Spring Cloud Order Service Discovery", new BigDecimal(0)),
                new OrderResponse(3, "Spring Cloud Order Client", new BigDecimal(0))
        );
    }
}
