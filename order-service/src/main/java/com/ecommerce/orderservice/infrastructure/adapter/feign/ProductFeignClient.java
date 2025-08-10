package com.ecommerce.orderservice.infrastructure.adapter.feign;

import com.ecommerce.orderservice.application.dto.ProductAvailabilityResponse;
import com.ecommerce.orderservice.application.dto.ProductResponse;
import com.ecommerce.orderservice.application.request.OrderItemRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service", path = "/products")
public interface ProductFeignClient {

    @GetMapping("/{id}")
    ProductResponse getProductById(@PathVariable("id") Long id, @RequestHeader("Authorization") String token);

    @PostMapping("/verify-availability")
    ProductAvailabilityResponse verifyProductAvailability(@RequestBody OrderItemRequest request, @RequestHeader("Authorization") String token);

    @PutMapping("/update-inventory/{id}/{updatedInventory}")
    void updateProductInventory(@PathVariable("id") Long id, @PathVariable("updatedInventory") int updatedInventory, @RequestHeader("Authorization") String token);
}
