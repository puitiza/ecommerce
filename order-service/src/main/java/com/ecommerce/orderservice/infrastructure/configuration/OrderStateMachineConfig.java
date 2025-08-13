package com.ecommerce.orderservice.infrastructure.configuration;

import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.port.OrderEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;

import java.util.EnumSet;

@Slf4j
@Configuration
@EnableStateMachineFactory
@RequiredArgsConstructor
public class OrderStateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderStatus, OrderEventType> {

    private final OrderEventPublisherPort eventPublisher;

    @Override
    public void configure(StateMachineStateConfigurer<OrderStatus, OrderEventType> states) throws Exception {
        states
                .withStates()
                .initial(OrderStatus.CREATED)
                .states(EnumSet.allOf(OrderStatus.class))
                .end(OrderStatus.FULFILLED)
                .end(OrderStatus.CANCELLED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStatus, OrderEventType> transitions) throws Exception {
        transitions
                // CREATED to VALIDATION_PENDING
                .withExternal()
                .source(OrderStatus.CREATED).target(OrderStatus.VALIDATION_PENDING)
                .event(OrderEventType.ORDER_CREATED)
                .action(validationPendingAction())  // Publish to product-service for validation/reservation
                .timerOnce(30000)  // 30s timeout to VALIDATION_FAILED

                // VALIDATION_PENDING to VALIDATION_SUCCEEDED
                .and().withExternal()
                .source(OrderStatus.VALIDATION_PENDING).target(OrderStatus.VALIDATION_SUCCEEDED)
                .event(OrderEventType.VALIDATION_SUCCEEDED)
                .action(validationSucceededAction())  // Publish PaymentStart

                // VALIDATION_PENDING to VALIDATION_FAILED
                .and().withExternal()
                .source(OrderStatus.VALIDATION_PENDING).target(OrderStatus.VALIDATION_FAILED)
                .event(OrderEventType.VALIDATION_FAILED)
                .action(validationFailedAction())  // Compensate: cancel/notify

                // VALIDATION_FAILED to VALIDATION_PENDING (retry)
                .and().withExternal()
                .source(OrderStatus.VALIDATION_FAILED).target(OrderStatus.VALIDATION_PENDING)
                .event(OrderEventType.RETRY_VALIDATION)
                .guard(retryGuard())  // Check retry count < 3
                .timerOnce(30000)

                // VALIDATION_SUCCEEDED to PAYMENT_PENDING
                .and().withExternal()
                .source(OrderStatus.VALIDATION_SUCCEEDED).target(OrderStatus.PAYMENT_PENDING)
                .event(OrderEventType.PAYMENT_START)
                .action(paymentPendingAction())  // Publish to payment-service
                .timerOnce(60000)  // 60s timeout

                // PAYMENT_PENDING to PAYMENT_SUCCEEDED
                .and().withExternal()
                .source(OrderStatus.PAYMENT_PENDING).target(OrderStatus.PAYMENT_SUCCEEDED)
                .event(OrderEventType.PAYMENT_SUCCEEDED)
                .action(paymentSucceededAction())  // Publish ShipmentStart

                // PAYMENT_PENDING to PAYMENT_FAILED
                .and().withExternal()
                .source(OrderStatus.PAYMENT_PENDING).target(OrderStatus.PAYMENT_FAILED)
                .event(OrderEventType.PAYMENT_FAILED)
                .action(paymentFailedAction())  // Compensate if no retry

                // PAYMENT_FAILED to PAYMENT_PENDING (retry)
                .and().withExternal()
                .source(OrderStatus.PAYMENT_FAILED).target(OrderStatus.PAYMENT_PENDING)
                .event(OrderEventType.RETRY_PAYMENT)
                .guard(retryGuard())
                .timerOnce(60000)

                // PAYMENT_SUCCEEDED to SHIPPING_PENDING
                .and().withExternal()
                .source(OrderStatus.PAYMENT_SUCCEEDED).target(OrderStatus.SHIPPING_PENDING)
                .event(OrderEventType.SHIPMENT_START)
                .action(shippingPendingAction())  // Publish to shipment-service
                .timerOnce(120000)  // 120s timeout

                // SHIPPING_PENDING to SHIPPING_SUCCEEDED
                .and().withExternal()
                .source(OrderStatus.SHIPPING_PENDING).target(OrderStatus.SHIPPING_SUCCEEDED)
                .event(OrderEventType.SHIPMENT_SUCCEEDED)
                .action(shippingSucceededAction())  // Transition to FULFILLED via Delivered

                // SHIPPING_PENDING to SHIPPING_FAILED
                .and().withExternal()
                .source(OrderStatus.SHIPPING_PENDING).target(OrderStatus.SHIPPING_FAILED)
                .event(OrderEventType.SHIPMENT_FAILED)
                .action(shippingFailedAction())

                // SHIPPING_FAILED to SHIPPING_PENDING (retry)
                .and().withExternal()
                .source(OrderStatus.SHIPPING_FAILED).target(OrderStatus.SHIPPING_PENDING)
                .event(OrderEventType.RETRY_SHIPMENT)
                .guard(retryGuard())
                .timerOnce(120000)

                // SHIPPING_SUCCEEDED to FULFILLED
                .and().withExternal()
                .source(OrderStatus.SHIPPING_SUCCEEDED).target(OrderStatus.FULFILLED)
                .event(OrderEventType.DELIVERED)
                .action(fulfilledAction())  // Optional: notify completion

                // Cancel transitions from various sources
                .and().withExternal()
                .source(OrderStatus.CREATED).target(OrderStatus.CANCELLED)
                .event(OrderEventType.CANCEL)
                .action(cancelAction())

                .and().withExternal()
                .source(OrderStatus.VALIDATION_PENDING).target(OrderStatus.CANCELLED)
                .event(OrderEventType.CANCEL)
                .action(cancelAction())

                .and().withExternal()
                .source(OrderStatus.PAYMENT_PENDING).target(OrderStatus.CANCELLED)
                .event(OrderEventType.CANCEL)
                .action(cancelAction())  // Includes refund

                .and().withExternal()
                .source(OrderStatus.SHIPPING_PENDING).target(OrderStatus.CANCELLED)
                .event(OrderEventType.CANCEL)
                .action(cancelAction());  // Includes refund + reverse shipment
    }

    // Action examples
    private Action<OrderStatus, OrderEventType> validationPendingAction() {
        return context -> {
            Order order = context.getExtendedState().get("order", Order.class);
            eventPublisher.publishOrderCreatedEvent(order);  // To product-service
            log.info("Validation pending for order {}", order.id());
        };
    }

    private Action<OrderStatus, OrderEventType> validationSucceededAction() {
        return context -> {
            Order order = context.getExtendedState().get("order", Order.class);
            eventPublisher.publishPaymentStartEvent(order);
            log.info("Validation succeeded for order {}", order.id());
        };
    }

    private Action<OrderStatus, OrderEventType> validationFailedAction() {
        return context -> {
            Order order = context.getExtendedState().get("order", Order.class);
            log.error("Validation failed for order {}", order.id());
            // Compensation: notify user, etc.
        };
    }

    private Action<OrderStatus, OrderEventType> paymentPendingAction() {
        return context -> {
            Order order = context.getExtendedState().get("order", Order.class);
            eventPublisher.publishPaymentStartEvent(order);  // Already triggered? Adjust if needed
            log.info("Payment pending for order {}", order.id());
        };
    }

    private Action<OrderStatus, OrderEventType> paymentSucceededAction() {
        return context -> {
            Order order = context.getExtendedState().get("order", Order.class);
            eventPublisher.publishShipmentStartEvent(order);
            log.info("Payment succeeded for order {}", order.id());
        };
    }

    private Action<OrderStatus, OrderEventType> paymentFailedAction() {
        return context -> {
            Order order = context.getExtendedState().get("order", Order.class);
            log.error("Payment failed for order {}", order.id());
            // Compensation if no retry
        };
    }

    private Action<OrderStatus, OrderEventType> shippingPendingAction() {
        return context -> {
            Order order = context.getExtendedState().get("order", Order.class);
            eventPublisher.publishShipmentStartEvent(order);
            log.info("Shipping pending for order {}", order.id());
        };
    }

    private Action<OrderStatus, OrderEventType> shippingSucceededAction() {
        return context -> {
            Order order = context.getExtendedState().get("order", Order.class);
            eventPublisher.publishDeliveredEvent(order);  // Or direct to FULFILLED
            log.info("Shipping succeeded for order {}", order.id());
        };
    }

    private Action<OrderStatus, OrderEventType> shippingFailedAction() {
        return context -> {
            Order order = context.getExtendedState().get("order", Order.class);
            log.error("Shipping failed for order {}", order.id());
            // Compensation
        };
    }

    private Action<OrderStatus, OrderEventType> fulfilledAction() {
        return context -> {
            Order order = context.getExtendedState().get("order", Order.class);
            log.info("Order fulfilled: {}", order.id());
            // Notify completion
        };
    }

    private Action<OrderStatus, OrderEventType> cancelAction() {
        return context -> {
            Order order = context.getExtendedState().get("order", Order.class);
            eventPublisher.publishOrderCancelledEvent(order);
            log.info("Order cancelled: {}", order.id());
            // Publish compensations: RefundRequest, RestockRequest, etc.
        };
    }

    private Guard<OrderStatus, OrderEventType> retryGuard() {
        return context -> {
            int retryCount = context.getExtendedState().get("retryCount", Integer.class, 0);
            if (retryCount < 3) {
                context.getExtendedState().set("retryCount", retryCount + 1);
                return true;
            }
            // No more retries, trigger compensation
            context.getStateMachine().sendEvent(OrderEventType.CANCEL);
            return false;
        };
    }
}