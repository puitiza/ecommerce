package com.ecommerce.orderservice.infrastructure.adapter.feign;

import com.ecommerce.orderservice.application.dto.BatchProductDetailsRequest;
import com.ecommerce.orderservice.application.dto.BatchProductDetailsResponse;
import com.ecommerce.orderservice.application.dto.BatchProductRequest;
import com.ecommerce.orderservice.application.dto.BatchProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

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
