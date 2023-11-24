package com.ecommerce.orderservice.clients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service")// "" is the name of the product service registered in Eureka
public interface ProductFeignClient {

	//@GetMapping("/products/{productId}")
	//Product getProductById(@PathVariable Long productId);
}
