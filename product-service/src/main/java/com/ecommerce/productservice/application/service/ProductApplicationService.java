package com.ecommerce.productservice.application.service;

import com.ecommerce.productservice.application.dto.ProductBatchValidationRequest;
import com.ecommerce.productservice.domain.port.in.ProductUseCase;

import java.util.UUID;

/**
 * Application service interface for product management, extending core use cases with event-driven logic.
 */
public interface ProductApplicationService extends ProductUseCase {

    /**
     * Validates and reserves inventory for a batch of products and publishes the result.
     *
     * @param request The batch validation request containing product IDs and quantities.
     * @param orderId The ID of the order associated with the reservation.
     */
    void validateAndReserveInventory(ProductBatchValidationRequest request, UUID orderId);
}
