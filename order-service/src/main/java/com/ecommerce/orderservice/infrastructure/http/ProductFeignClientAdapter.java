package com.ecommerce.orderservice.infrastructure.http;

import com.ecommerce.orderservice.application.dto.ProductAvailabilityResponse;
import com.ecommerce.orderservice.application.dto.ProductResponse;
import com.ecommerce.orderservice.application.port.out.ProductServicePort;
import com.ecommerce.orderservice.application.request.OrderItemRequest;
import com.ecommerce.orderservice.domain.exception.OrderValidationException;
import com.ecommerce.orderservice.infrastructure.feign.ProductFeignClient;
import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductFeignClientAdapter implements ProductServicePort {
    private final ProductFeignClient productFeignClient;

    @Override
    @CircuitBreaker(name = "productServiceCircuit", fallbackMethod = "verifyAvailabilityFallback")
    public ProductAvailabilityResponse verifyProductAvailability(OrderItemRequest request, String token) {
        return productFeignClient.verifyProductAvailability(request, token);
    }

    @Override
    @CircuitBreaker(name = "productServiceCircuit", fallbackMethod = "getProductFallback")
    public ProductResponse getProductById(Long id, String token) {
        return productFeignClient.getProductById(id, token);
    }

    @Override
    @CircuitBreaker(name = "productServiceCircuit", fallbackMethod = "updateInventoryFallback")
    public void updateProductInventory(Long id, int updatedInventory, String token) {
        productFeignClient.updateProductInventory(id, updatedInventory, token);
    }

    private ProductAvailabilityResponse verifyAvailabilityFallback(OrderItemRequest request, String token, Throwable t) {
        throw handleProductError(request.productId(), t, "Failed to verify product availability");
    }

    private ProductResponse getProductFallback(Long id, String token, Throwable t) {
        throw handleProductError(id, t, "Failed to retrieve product");
    }

    private void updateInventoryFallback(Long id, int updatedInventory, String token, Throwable t) {
        throw handleProductError(id, t, "Failed to update product inventory");
    }

    private RuntimeException handleProductError(Long id, Throwable t, String defaultMessage) {
        log.error("{} for Id {}: {}", defaultMessage, id, t.getMessage());
        String details = t.getMessage() != null ? t.getMessage() : String.format("%s for productId %d", defaultMessage, id);
        if (t instanceof FeignException feignException) {
            int status = feignException.status();
            return switch (status) {
                case 404 -> new ResourceNotFoundException("Product", id.toString());
                case 429 -> new OrderValidationException(
                        ExceptionError.GATEWAY_RATE_LIMIT,
                        String.format("Rate limit exceeded for productId %d", id),
                        id
                );
                case 503, -1 -> new OrderValidationException(
                        ExceptionError.SERVICE_UNAVAILABLE,
                        details,
                        "Product",
                        id
                );
                default -> new OrderValidationException(
                        ExceptionError.ORDER_PRODUCT_AVAILABILITY_CHECK_FAILED,
                        details,
                        id
                );
            };
        } else if (t instanceof CallNotPermittedException) {
            return new OrderValidationException(
                    ExceptionError.SERVICE_UNAVAILABLE,
                    details,
                    "Product",
                    id
            );
        }
        return new OrderValidationException(
                ExceptionError.ORDER_PRODUCT_AVAILABILITY_CHECK_FAILED,
                details,
                id
        );
    }
}