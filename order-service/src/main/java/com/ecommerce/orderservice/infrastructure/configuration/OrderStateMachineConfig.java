package com.ecommerce.orderservice.infrastructure.configuration;

import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import com.ecommerce.orderservice.domain.port.OrderEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import reactor.core.publisher.Mono;

import java.util.EnumSet;
import java.util.Optional;

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
                // CREATED -> VALIDATION_PENDING (Initial transition)
                .withExternal()
                .source(OrderStatus.CREATED).target(OrderStatus.VALIDATION_PENDING)
                .event(OrderEventType.ORDER_CREATED)
                .action(publishOrderCreatedEvent())
                .timerOnce(30000)

                // VALIDATION_PENDING -> VALIDATION_SUCCEEDED
                .and().withExternal()
                .source(OrderStatus.VALIDATION_PENDING).target(OrderStatus.VALIDATION_SUCCEEDED)
                .event(OrderEventType.VALIDATION_SUCCEEDED)
                .action(publishPaymentStartEvent())

                // VALIDATION_PENDING -> VALIDATION_FAILED
                .and().withExternal()
                .source(OrderStatus.VALIDATION_PENDING).target(OrderStatus.VALIDATION_FAILED)
                .event(OrderEventType.VALIDATION_FAILED)
                .action(validationFailedAction())

                // VALIDATION_FAILED -> VALIDATION_PENDING (Retry)
                .and().withExternal()
                .source(OrderStatus.VALIDATION_FAILED).target(OrderStatus.VALIDATION_PENDING)
                .event(OrderEventType.RETRY_VALIDATION)
                .guard(validationRetryGuard())
                .timerOnce(30000)

                // VALIDATION_SUCCEEDED -> PAYMENT_PENDING
                .and().withExternal()
                .source(OrderStatus.VALIDATION_SUCCEEDED).target(OrderStatus.PAYMENT_PENDING)
                .event(OrderEventType.PAYMENT_START)
                .action(publishPaymentStartEvent())
                .timerOnce(60000)

                // PAYMENT_PENDING -> PAYMENT_SUCCEEDED
                .and().withExternal()
                .source(OrderStatus.PAYMENT_PENDING).target(OrderStatus.PAYMENT_SUCCEEDED)
                .event(OrderEventType.PAYMENT_SUCCEEDED)
                .action(publishShipmentStartEvent())

                // PAYMENT_PENDING -> PAYMENT_FAILED
                .and().withExternal()
                .source(OrderStatus.PAYMENT_PENDING).target(OrderStatus.PAYMENT_FAILED)
                .event(OrderEventType.PAYMENT_FAILED)
                .action(paymentFailedAction())

                // PAYMENT_FAILED -> PAYMENT_PENDING (retry)
                .and().withExternal()
                .source(OrderStatus.PAYMENT_FAILED).target(OrderStatus.PAYMENT_PENDING)
                .event(OrderEventType.RETRY_PAYMENT)
                .guard(paymentRetryGuard())
                .timerOnce(60000)

                // PAYMENT_SUCCEEDED -> SHIPPING_PENDING
                .and().withExternal()
                .source(OrderStatus.PAYMENT_SUCCEEDED).target(OrderStatus.SHIPPING_PENDING)
                .event(OrderEventType.SHIPMENT_START)
                .action(publishShipmentStartEvent())
                .timerOnce(120000)

                // SHIPPING_PENDING -> SHIPPING_SUCCEEDED
                .and().withExternal()
                .source(OrderStatus.SHIPPING_PENDING).target(OrderStatus.SHIPPING_SUCCEEDED)
                .event(OrderEventType.SHIPMENT_SUCCEEDED)
                .action(publishDeliveredEvent())

                // SHIPPING_PENDING -> SHIPPING_FAILED
                .and().withExternal()
                .source(OrderStatus.SHIPPING_PENDING).target(OrderStatus.SHIPPING_FAILED)
                .event(OrderEventType.SHIPMENT_FAILED)
                .action(shipmentFailedAction())

                // SHIPPING_FAILED -> SHIPPING_PENDING (retry)
                .and().withExternal()
                .source(OrderStatus.SHIPPING_FAILED).target(OrderStatus.SHIPPING_PENDING)
                .event(OrderEventType.RETRY_SHIPMENT)
                .guard(shipmentRetryGuard())
                .timerOnce(120000)

                // SHIPPING_SUCCEEDED -> FULFILLED
                .and().withExternal()
                .source(OrderStatus.SHIPPING_SUCCEEDED).target(OrderStatus.FULFILLED)
                .event(OrderEventType.DELIVERED)
                .action(fulfilledAction()) // Optional: notify completion

                // Cancel transitions from various sources
                .and().withExternal()
                .source(OrderStatus.CREATED).target(OrderStatus.CANCELLED)
                .event(OrderEventType.CANCEL)
                .action(publishCancelEvent())

                .and().withExternal()
                .source(OrderStatus.VALIDATION_PENDING).target(OrderStatus.CANCELLED)
                .event(OrderEventType.CANCEL)
                .action(publishCancelEvent())

                .and().withExternal()
                .source(OrderStatus.PAYMENT_PENDING).target(OrderStatus.CANCELLED)
                .event(OrderEventType.CANCEL)
                .action(publishCancelEvent()) // Includes refund

                .and().withExternal()
                .source(OrderStatus.SHIPPING_PENDING).target(OrderStatus.CANCELLED)
                .event(OrderEventType.CANCEL)
                .action(publishCancelEvent()); // Includes refund + reverse shipment
    }

    private Optional<Order> getOrderFromContext(StateContext<OrderStatus, OrderEventType> context) {
        return Optional.ofNullable(context.getMessageHeader("order"))
                .filter(Order.class::isInstance)
                .map(Order.class::cast);
    }

    private Guard<OrderStatus, OrderEventType> createRetryGuard(String retryCountKey) {
        return context -> {
            int retryCount = (int) context.getExtendedState().getVariables().getOrDefault(retryCountKey, 0);
            if (retryCount < 3) {
                context.getExtendedState().getVariables().put(retryCountKey, retryCount + 1);
                return true;
            }
            Message<OrderEventType> cancelMessage = MessageBuilder.withPayload(OrderEventType.CANCEL)
                    .setHeader("order", context.getMessageHeader("order"))
                    .build();
            context.getStateMachine().sendEvent(Mono.just(cancelMessage)).subscribe();
            return false;
        };
    }

    // Event publication actions, using the Helper Get Order From Context
    @Bean
    public Action<OrderStatus, OrderEventType> publishOrderCreatedEvent() {
        return context -> getOrderFromContext(context)
                .ifPresentOrElse(order -> {
                    eventPublisher.publishOrderCreatedEvent(order);
                    log.info("Published OrderCreated event for order ID: {}", order.id());
                }, () -> log.warn("Null or invalid order in publishOrderCreatedEvent"));
    }

    @Bean
    public Action<OrderStatus, OrderEventType> publishPaymentStartEvent() {
        return context -> getOrderFromContext(context)
                .ifPresentOrElse(order -> {
                    eventPublisher.publishPaymentStartEvent(order);
                    log.info("Published PaymentStart event for order ID: {}", order.id());
                }, () -> log.warn("Null or invalid order in publishPaymentStartEvent"));
    }

    @Bean
    public Action<OrderStatus, OrderEventType> publishShipmentStartEvent() {
        return context -> getOrderFromContext(context)
                .ifPresentOrElse(order -> {
                    eventPublisher.publishShipmentStartEvent(order);
                    log.info("Published ShipmentStart event for order ID: {}", order.id());
                }, () -> log.warn("Null or invalid order in publishShipmentStartEvent"));
    }

    @Bean
    public Action<OrderStatus, OrderEventType> publishDeliveredEvent() {
        return context -> getOrderFromContext(context)
                .ifPresentOrElse(order -> {
                    eventPublisher.publishDeliveredEvent(order);
                    log.info("Published Delivered event for order ID: {}", order.id());
                }, () -> log.warn("Null or invalid order in publishDeliveredEvent"));
    }

    @Bean
    public Action<OrderStatus, OrderEventType> publishCancelEvent() {
        return context -> getOrderFromContext(context)
                .ifPresentOrElse(order -> {
                    eventPublisher.publishCancelEvent(order);
                    log.info("Published Cancel event for order ID: {}", order.id());
                }, () -> log.warn("Null or invalid order in publishCancelEvent"));
    }

    // Error Log actions and completed
    @Bean
    public Action<OrderStatus, OrderEventType> validationFailedAction() {
        return context -> getOrderFromContext(context)
                .ifPresent(order -> log.error("Validation failed for order {}", order.id()));
    }

    @Bean
    public Action<OrderStatus, OrderEventType> paymentFailedAction() {
        return context -> getOrderFromContext(context)
                .ifPresent(order -> log.error("Payment failed for order {}", order.id()));
    }

    @Bean
    public Action<OrderStatus, OrderEventType> shipmentFailedAction() {
        return context -> getOrderFromContext(context)
                .ifPresent(order -> log.error("Shipment failed for order {}", order.id()));
    }

    @Bean
    public Action<OrderStatus, OrderEventType> fulfilledAction() {
        return context -> getOrderFromContext(context)
                .ifPresent(order -> log.info("Order fulfilled: {}", order.id()));
    }

    // Guards retry
    @Bean
    public Guard<OrderStatus, OrderEventType> validationRetryGuard() {
        return createRetryGuard("validationRetryCount");
    }

    @Bean
    public Guard<OrderStatus, OrderEventType> paymentRetryGuard() {
        return createRetryGuard("paymentRetryCount");
    }

    @Bean
    public Guard<OrderStatus, OrderEventType> shipmentRetryGuard() {
        return createRetryGuard("shippingRetryCount");
    }
}