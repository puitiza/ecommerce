package com.ecommerce.productservice.domain.port.out;

import com.ecommerce.productservice.domain.event.ProductEventType;
import com.ecommerce.productservice.domain.model.Product;

import java.util.UUID;

/**
 * Outbound port for publishing product-related events.
 */
public interface ProductEventPublisherPort {
    void publish(Product product, ProductEventType eventType);

    void publishValidationSucceeded(UUID orderId);

    void publishValidationFailed(UUID orderId);
}