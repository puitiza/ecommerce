package com.ecommerce.orderservice.clients;


import com.ecommerce.orderservice.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product-service")// "" is the name of the product service registered in Eureka
public interface ProductFeignClient {

	@GetMapping("/products/byOrderId")
	List<Product> getProductsByOrderId(@RequestParam Long orderId);
}
