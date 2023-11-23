package com.ecommerce.productservice.controller;


import com.ecommerce.productservice.model.ProductResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@RestController
public class ProductController {
    @GetMapping("/product")
    public List<ProductResponse> getOrders(){

        return Arrays.asList(
                new ProductResponse(1, "Spring ", new BigDecimal(0)),
                new ProductResponse(2, "Spring Cloud Eureka Service Discovery", new BigDecimal(0)),
                new ProductResponse(3, "Spring Cloud Eureka Client", new BigDecimal(0))
        );
    }
}
