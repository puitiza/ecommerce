package com.ecommerce.orderservice.infrastructure.adapter.feign;

import com.ecommerce.orderservice.application.dto.BatchProductDetailsRequest;
import com.ecommerce.orderservice.application.dto.BatchProductDetailsResponse;
import com.ecommerce.orderservice.application.dto.BatchProductRequest;
import com.ecommerce.orderservice.application.dto.BatchProductResponse;
import com.ecommerce.orderservice.application.port.out.ProductServicePort;
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
    @CircuitBreaker(name = "productServiceCircuit")
    public BatchProductResponse verifyAndGetProducts(BatchProductRequest items, String token) {
        return productFeignClient.verifyAndGetProducts(items, token);
    }

    @Override
    @CircuitBreaker(name = "productServiceCircuit")
    public List<BatchProductDetailsResponse> getProductsDetailsInBatch(BatchProductDetailsRequest request, String token) {
        return productFeignClient.getProductsDetailsInBatch(request, token);
    }

    /*@SuppressWarnings("unused")
    private ProductResponse getProductFallback(Long id, String token, Throwable t) {
        throw handleError(1L, t, "Failed to retrieve product");
    }

    @SuppressWarnings("unused")
    private void updateInventoryFallback(Long id, int updatedInventory, String token, Throwable t) {
        throw handleError(2L, t, "Failed to update product inventory");
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
     */
}