package com.ecommerce.orderservice.feign;


import com.ecommerce.orderservice.model.dto.ProductDto;
import com.ecommerce.orderservice.model.request.OrderItemRequest;
import com.ecommerce.orderservice.model.response.ProductAvailabilityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "product-service")
public interface ProductFeignClient {

    @GetMapping("/products/{id}")
    ProductDto getProductById(@PathVariable Long id,
                              @RequestHeader("Authorization") String authorizationHeader);

    @PostMapping("/products/verify-availability")
    ProductAvailabilityResponse verifyProductAvailability(@RequestBody OrderItemRequest orderItemRequest,
                                                          @RequestHeader("Authorization") String authorizationHeader);

    @PutMapping("/products/update-inventory/{id}/{updatedInventory}")
    void updateProductInventory(@PathVariable Long id, @PathVariable int updatedInventory,
                                @RequestHeader("Authorization") String authorizationHeader);


}
