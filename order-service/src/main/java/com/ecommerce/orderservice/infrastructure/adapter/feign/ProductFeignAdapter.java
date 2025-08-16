package com.ecommerce.orderservice.infrastructure.adapter.feign;

import com.ecommerce.orderservice.application.dto.*;
import com.ecommerce.orderservice.application.port.out.ProductServicePort;
import com.ecommerce.orderservice.domain.exception.OrderValidationException;
import com.ecommerce.shared.exception.ExceptionError;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductFeignAdapter implements ProductServicePort {

    private static final String SERVICE_NAME = "Product";
    private final ProductFeignClient productFeignClient;

    @Override
    @CircuitBreaker(name = "productServiceCircuit", fallbackMethod = "verifyAndGetProductsFallback")
    public BatchProductResponse verifyAndGetProducts(BatchProductRequest items, String token) {
        return productFeignClient.verifyAndGetProducts(items, token);
    }

    @Override
    @CircuitBreaker(name = "productServiceCircuit", fallbackMethod = "getProductsDetailsInBatchFallback")
    public List<BatchProductDetailsResponse> getProductsDetailsInBatch(BatchProductDetailsRequest request, String token) {
        return productFeignClient.getProductsDetailsInBatch(request, token);
    }

    @SuppressWarnings("unused")
    private BatchProductResponse verifyAndGetProductsFallback(BatchProductRequest items, String token, Throwable t) {
        List<Long> productIds = items.items().stream().map(OrderItemRequest::productId).toList();
        String errorMessage = "Failed to verify and retrieve products in batch";
        throw handleError(errorMessage, productIds, t);
    }

    @SuppressWarnings("unused")
    private List<BatchProductDetailsResponse> getProductsDetailsInBatchFallback(BatchProductDetailsRequest request, String token, Throwable t) {
        String errorMessage = "Failed to retrieve product details in batch";
        throw handleError(errorMessage, request.productIds(), t);
    }

    private OrderValidationException handleError(String errorMessage, List<Long> productIds, Throwable t) {
        String details = t.getMessage() != null ? t.getMessage() : errorMessage + " for products: " + productIds;
        String productIdsStr = productIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        log.error("{} for products {}: {}", errorMessage, productIds, t.getMessage(), t);

        if (t instanceof FeignException feign) {
            switch (feign.status()) {
                case 503, -1 -> {
                    return new OrderValidationException(ExceptionError.SERVICE_UNAVAILABLE, details, SERVICE_NAME, productIdsStr);
                }
                default -> {
                    return new OrderValidationException(ExceptionError.ORDER_PRODUCT_AVAILABILITY_CHECK_FAILED,
                            details, productIdsStr
                    );
                }
            }
        }
        return new OrderValidationException(ExceptionError.INTERNAL_SERVER_ERROR, details);
    }
}