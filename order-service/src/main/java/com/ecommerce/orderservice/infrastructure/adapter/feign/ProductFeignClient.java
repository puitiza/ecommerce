package com.ecommerce.orderservice.infrastructure.adapter.feign;

import com.ecommerce.orderservice.application.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@FeignClient(name = "product-service", path = "/products")
public interface ProductFeignClient {

    @PostMapping("/batch")
    BatchProductResponse verifyAndGetProducts(@RequestBody BatchProductRequest items,
                                              @RequestHeader("Authorization") String token);

    @PostMapping("/details")
    List<BatchProductDetailsResponse> getProductsDetailsInBatch(@RequestBody BatchProductDetailsRequest request,
                                                                @RequestHeader("Authorization") String token);
}
