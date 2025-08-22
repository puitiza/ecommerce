package com.ecommerce.productservice.application.service;

import com.ecommerce.productservice.domain.event.ProductEventType;
import com.ecommerce.productservice.domain.model.Product;
import com.ecommerce.productservice.domain.port.in.ProductUseCase;

/**
 * Application service interface for product management, extending core use cases with event-driven logic.
 */
public interface ProductApplicationService extends ProductUseCase {

    /**
     * Publishes a product-related event to notify other services.
     *
     * @param product   the product involved in the event
     * @param eventType the type of event to publish
     */
    void publishProductEvent(Product product, ProductEventType eventType);
}
