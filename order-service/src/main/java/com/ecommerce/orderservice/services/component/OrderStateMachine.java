package com.ecommerce.orderservice.services.component;

import com.ecommerce.orderservice.model.entity.OrderStatus;
import com.ecommerce.orderservice.model.event.OrderEventType;
import org.springframework.stereotype.Component;

@Component
public class OrderStateMachine {
    private final OrderStatus[][] transitions = {
            /* CREATED */         {OrderStatus.VALIDATING, OrderStatus.CANCELLED},
            /* VALIDATING */      {OrderStatus.PAYMENT_PENDING, OrderStatus.VALIDATION_FAILED, OrderStatus.VALIDATION_SUCCEEDED, OrderStatus.CANCELLED},
            /* PAYMENT_PENDING */ {OrderStatus.FULFILLING, OrderStatus.CANCELLED},
            /* FULFILLING */      {OrderStatus.FULFILLED, OrderStatus.CANCELLED},
            /* FULFILLED */       {},
            /* CANCELLED */       {}
    };

    // Check if a transition is allowed
    public boolean canTransition(OrderStatus source, OrderEventType event) {
        int sourceIndex = getOrderStatusIndex(source);
        int eventIndex = getEventIndex(event);

        // Check if the transition is defined
        return sourceIndex >= 0 && eventIndex >= 0 && transitions[sourceIndex][eventIndex] != null;
    }

    // Check if cancellation is allowed
    public boolean canCancel(OrderStatus source) {
        int sourceIndex = getOrderStatusIndex(source);

        // Check if cancellation is allowed from the current state
        return sourceIndex >= 0 && transitions[sourceIndex][OrderEventType.ORDER_VALIDATED_FAILED.ordinal()] == OrderStatus.CANCELLED;
    }

    // Helper method to get the index of an order status in the transition matrix
    private int getOrderStatusIndex(OrderStatus status) {
        OrderStatus[] values = OrderStatus.values();
        for (int i = 0; i < values.length; i++) {
            if (values[i] == status) {
                return i;
            }
        }
        return -1;
    }

    // Helper method to get the index of an event in the transition matrix
    private int getEventIndex(OrderEventType event) {
        return event.ordinal();
    }
}
