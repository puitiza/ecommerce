package com.ecommerce.orderservice.feign;


import com.ecommerce.orderservice.model.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "product-service", url = "http://localhost:8002")
public interface ProductFeignClient {

    @GetMapping("/products/{id}")
    ProductDto getProductById(@PathVariable Long id, @RequestHeader("Authorization") String authorizationHeader);

}
