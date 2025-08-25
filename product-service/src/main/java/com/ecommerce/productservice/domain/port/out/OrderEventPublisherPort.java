package com.ecommerce.productservice.domain.port.out;

import com.ecommerce.shared.domain.event.OrderEventPayload;

public interface OrderEventPublisherPort {
    void publishValidationSucceeded(OrderEventPayload order);

    void publishValidationFailed(OrderEventPayload order);
}
