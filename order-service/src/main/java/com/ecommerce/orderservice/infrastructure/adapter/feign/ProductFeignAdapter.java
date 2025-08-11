package com.ecommerce.orderservice.infrastructure.adapter.feign;

import com.ecommerce.orderservice.application.dto.ProductAvailabilityResponse;
import com.ecommerce.orderservice.application.dto.ProductResponse;
import com.ecommerce.orderservice.application.port.out.ProductServicePort;
import com.ecommerce.orderservice.application.dto.OrderItemRequest;
import com.ecommerce.orderservice.domain.exception.OrderValidationException;
import com.ecommerce.shared.exception.ExceptionError;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductFeignAdapter implements ProductServicePort {
    private static final String SERVICE_NAME = "Product";
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

    @SuppressWarnings("unused")
    private ProductAvailabilityResponse verifyAvailabilityFallback(OrderItemRequest request, String token, Throwable t) {
        throw handleProductError(request.productId(), t, "Failed to verify product availability");
    }

    @SuppressWarnings("unused")
    private ProductResponse getProductFallback(Long id, String token, Throwable t) {
        throw handleProductError(id, t, "Failed to retrieve product");
    }

    @SuppressWarnings("unused")
    private void updateInventoryFallback(Long id, int updatedInventory, String token, Throwable t) {
        throw handleProductError(id, t, "Failed to update product inventory");
    }

    private RuntimeException handleProductError(Long id, Throwable t, String defaultMessage) {
        log.error("{} for {}Id {}: {}", defaultMessage, SERVICE_NAME, id, t.getMessage());
        String details = t.getMessage() != null ? t.getMessage() : String.format("%s for %sId %d", defaultMessage, SERVICE_NAME, id);
        if (t instanceof FeignException feignException) {
            return switch (feignException.status()) {
                case 404 -> new ResourceNotFoundException(SERVICE_NAME, id.toString());
                case 429 -> new OrderValidationException(ExceptionError.GATEWAY_RATE_LIMIT,
                        String.format("Rate limit exceeded for %sId %d", SERVICE_NAME, id));
                case 503, -1 -> new OrderValidationException(ExceptionError.SERVICE_UNAVAILABLE,
                        details,
                        SERVICE_NAME, id);
                default -> new OrderValidationException(ExceptionError.ORDER_PRODUCT_AVAILABILITY_CHECK_FAILED,
                        details, id);
            };
        }
        return new OrderValidationException(ExceptionError.INTERNAL_SERVER_ERROR, details);
    }
}