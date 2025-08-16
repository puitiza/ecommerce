package com.ecommerce.orderservice.infrastructure.adapter.feign;

import com.ecommerce.orderservice.application.dto.*;
import com.ecommerce.orderservice.application.port.out.ProductServicePort;
import com.ecommerce.orderservice.domain.exception.OrderValidationException;
import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductFeignAdapter implements ProductServicePort {
    private static final String SERVICE_NAME = "Product";
    private final ProductFeignClient productFeignClient;

    @Override
    //@CircuitBreaker(name = "productServiceCircuit", fallbackMethod = "verifyAvailabilityFallback")
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

    @Override
    @CircuitBreaker(name = "productServiceCircuit", fallbackMethod = "verifyAvailabilityFallback")
    public BatchProductResponse verifyAndGetProducts(BatchProductRequest items, String token) {
        return productFeignClient.verifyAndGetProducts(items, token);
    }

    @Override
    //@CircuitBreaker(name = "productServiceCircuit", fallbackMethod = "getProductFallback")
    public List<BatchProductDetailsResponse> getProductsDetailsInBatch(BatchProductDetailsRequest request, String token) {
        return productFeignClient.getProductsDetailsInBatch(request, token);
    }

    @SuppressWarnings("unused")
    private ProductAvailabilityResponse verifyAvailabilityFallback(OrderItemRequest request, String token, Throwable t) {
        throw handleError(request.productId(), t, "Failed to verify product availability");
    }

    @SuppressWarnings("unused")
    private ProductResponse getProductFallback(Long id, String token, Throwable t) {
        throw handleError(id, t, "Failed to retrieve product");
    }

    @SuppressWarnings("unused")
    private void updateInventoryFallback(Long id, int updatedInventory, String token, Throwable t) {
        throw handleError(id, t, "Failed to update product inventory");
    }

    private RuntimeException handleError(Long id, Throwable t, String msg) {
        log.error("{} for ID {}: {}", msg, id, t.getMessage(), t);
        String details = t.getMessage() != null ? t.getMessage() : msg + " for ID " + id;
        if (t instanceof FeignException feign) {
            return switch (feign.status()) {
                case 404 -> new ResourceNotFoundException(SERVICE_NAME, id.toString());
                case 429 -> new OrderValidationException(ExceptionError.GATEWAY_RATE_LIMIT,
                        "Rate limit exceeded for ID " + id);
                case 503, -1 -> new OrderValidationException(ExceptionError.SERVICE_UNAVAILABLE,
                        details, SERVICE_NAME, id);
                default -> new OrderValidationException(ExceptionError.ORDER_PRODUCT_AVAILABILITY_CHECK_FAILED,
                        details, id);
            };
        }
        return new OrderValidationException(ExceptionError.INTERNAL_SERVER_ERROR, details);
    }
}