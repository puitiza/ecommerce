package com.ecommerce.orderservice.infrastructure.configuration;

import com.ecommerce.orderservice.domain.event.OrderEventType;
import com.ecommerce.orderservice.domain.model.Order;
import com.ecommerce.orderservice.domain.model.OrderStatus;
import com.ecommerce.orderservice.domain.port.out.OrderEventPublisherPort;
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
import org.springframework.statemachine.persist.DefaultStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;
import reactor.core.publisher.Mono;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Configures the state machine for order processing, defining states, transitions, actions, and guards.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableStateMachineFactory
public class OrderStateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderStatus, OrderEventType> {

    private final OrderEventPublisherPort eventPublisher;
    private final OrderStateMachinePersister delegatePersister;

    /**
     * Configures the states of the order state machine, including initial, intermediate, and end states.
     *
     * @param states the state configurer
     * @throws Exception if configuration fails
     */
    @Override
    public void configure(StateMachineStateConfigurer<OrderStatus, OrderEventType> states) throws Exception {
        states
                .withStates()
                .initial(OrderStatus.CREATED)
                .states(EnumSet.allOf(OrderStatus.class))
                .end(OrderStatus.FULFILLED)
                .end(OrderStatus.CANCELLED);
    }

    /**
     * Configures transitions between states, including events, actions, guards, and timers.
     *
     * @param transitions the transition configurer
     * @throws Exception if configuration fails
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStatus, OrderEventType> transitions) throws Exception {
        transitions
                // ---- CREATED STATE ----
                /*
                // CREATED -> VALIDATION_PENDING
                .withExternal()
                .source(OrderStatus.CREATED).target(OrderStatus.VALIDATION_PENDING)
                .event(OrderEventType.ORDER_CREATED)
                .action(publishEvent(OrderEventType.ORDER_CREATED))
                .timerOnce(30000)

                // CREATED -> CREATED (Update)
                .and().withExternal()
                .source(OrderStatus.CREATED).target(OrderStatus.CREATED)
                .event(OrderEventType.ORDER_UPDATED)
                .action(publishEvent(OrderEventType.ORDER_UPDATED))
                */
                .withExternal() //(Manual confirmation)
                .source(OrderStatus.CREATED).target(OrderStatus.VALIDATION_PENDING)
                .event(OrderEventType.ORDER_CREATED)
                .action(publishEvent(OrderEventType.ORDER_CREATED))

                .and().withExternal() //(Automatic after 5 minutes)
                .source(OrderStatus.CREATED).target(OrderStatus.VALIDATION_PENDING)
                .event(OrderEventType.AUTO_VALIDATE)
                .action(publishEvent(OrderEventType.AUTO_VALIDATE))

                //.and().withExternal() //(Update with timer reset)
                //.source(OrderStatus.CREATED).target(OrderStatus.CREATED)
                //.event(OrderEventType.ORDER_UPDATED)
                //.action(publishEvent(OrderEventType.ORDER_UPDATED))

                // ---- VALIDATION ----
                .and().withExternal()
                .source(OrderStatus.VALIDATION_PENDING).target(OrderStatus.VALIDATION_SUCCEEDED)
                .event(OrderEventType.VALIDATION_SUCCEEDED)
                .action(publishEvent(OrderEventType.PAYMENT_START))

                .and().withExternal()
                .source(OrderStatus.VALIDATION_PENDING).target(OrderStatus.VALIDATION_FAILED)
                .event(OrderEventType.VALIDATION_FAILED)
                .action(validationFailedAction())

                .and().withExternal()
                .source(OrderStatus.VALIDATION_FAILED).target(OrderStatus.VALIDATION_PENDING)
                .event(OrderEventType.RETRY_VALIDATION)
                .guard(validationRetryGuard())
                .action(publishEvent(OrderEventType.RETRY_VALIDATION))
                .timerOnce(30000)

                .and().withExternal()
                .source(OrderStatus.VALIDATION_FAILED).target(OrderStatus.CANCELLED)
                .event(OrderEventType.AUTO_CANCEL)
                .action(publishEvent(OrderEventType.CANCEL))

                // ---- PAYMENT ----
                .and().withExternal()
                .source(OrderStatus.VALIDATION_SUCCEEDED).target(OrderStatus.PAYMENT_PENDING)
                .event(OrderEventType.PAYMENT_START)
                .action(publishEvent(OrderEventType.PAYMENT_START))
                .timerOnce(60000)

                .and().withExternal()
                .source(OrderStatus.PAYMENT_PENDING).target(OrderStatus.PAYMENT_SUCCEEDED)
                .event(OrderEventType.PAYMENT_SUCCEEDED)
                .action(publishEvent(OrderEventType.SHIPMENT_START))

                .and().withExternal()
                .source(OrderStatus.PAYMENT_PENDING).target(OrderStatus.PAYMENT_FAILED)
                .event(OrderEventType.PAYMENT_FAILED)
                .action(paymentFailedAction())

                .and().withExternal()
                .source(OrderStatus.PAYMENT_FAILED).target(OrderStatus.PAYMENT_PENDING)
                .event(OrderEventType.RETRY_PAYMENT)
                .guard(paymentRetryGuard())
                .action(publishEvent(OrderEventType.RETRY_PAYMENT))
                .timerOnce(60000)

                .and().withExternal()
                .source(OrderStatus.PAYMENT_FAILED).target(OrderStatus.CANCELLED)
                .event(OrderEventType.AUTO_CANCEL)
                .action(publishEvent(OrderEventType.CANCEL))

                // ---- SHIPPING ----
                .and().withExternal()
                .source(OrderStatus.PAYMENT_SUCCEEDED).target(OrderStatus.SHIPPING_PENDING)
                .event(OrderEventType.SHIPMENT_START)
                .action(publishEvent(OrderEventType.SHIPMENT_START))
                .timerOnce(120000)

                .and().withExternal()
                .source(OrderStatus.SHIPPING_PENDING).target(OrderStatus.SHIPPING_SUCCEEDED)
                .event(OrderEventType.SHIPMENT_SUCCEEDED)
                .action(publishEvent(OrderEventType.DELIVERED))

                .and().withExternal()
                .source(OrderStatus.SHIPPING_PENDING).target(OrderStatus.SHIPPING_FAILED)
                .event(OrderEventType.SHIPMENT_FAILED)
                .action(shipmentFailedAction())

                .and().withExternal()
                .source(OrderStatus.SHIPPING_FAILED).target(OrderStatus.SHIPPING_PENDING)
                .event(OrderEventType.RETRY_SHIPMENT)
                .guard(shipmentRetryGuard())
                .action(publishEvent(OrderEventType.RETRY_SHIPMENT))
                .timerOnce(120000)

                .and().withExternal()
                .source(OrderStatus.SHIPPING_FAILED).target(OrderStatus.CANCELLED)
                .event(OrderEventType.AUTO_CANCEL)
                .action(publishEvent(OrderEventType.CANCEL))

                .and().withExternal()
                .source(OrderStatus.SHIPPING_SUCCEEDED).target(OrderStatus.FULFILLED)
                .event(OrderEventType.DELIVERED)
                .action(fulfilledAction())

                // ---- CANCEL TRANSITIONS ----
                .and().withExternal()
                .source(OrderStatus.CREATED).target(OrderStatus.CANCELLED)
                .event(OrderEventType.CANCEL)
                .action(publishEvent(OrderEventType.CANCEL))

                .and().withExternal()
                .source(OrderStatus.VALIDATION_PENDING).target(OrderStatus.CANCELLED)
                .event(OrderEventType.CANCEL)
                .action(publishEvent(OrderEventType.CANCEL))

                .and().withExternal()
                .source(OrderStatus.PAYMENT_PENDING).target(OrderStatus.CANCELLED)
                .event(OrderEventType.CANCEL)
                .action(publishEvent(OrderEventType.CANCEL))

                .and().withExternal()
                .source(OrderStatus.SHIPPING_PENDING).target(OrderStatus.CANCELLED)
                .event(OrderEventType.CANCEL)
                .action(publishEvent(OrderEventType.CANCEL));
    }

    /**
     * Creates an action to publish events to the event publisher based on the event type.
     *
     * @param eventType the type of event to publish
     * @return the action to execute
     */
    private Action<OrderStatus, OrderEventType> publishEvent(OrderEventType eventType) {
        return context -> getOrderFromContext(context).ifPresentOrElse(
                order -> {
                    var eventPayload = order.toEventPayload();
                    switch (eventType) {
                        case ORDER_CREATED -> eventPublisher.publishOrderCreatedEvent(eventPayload);
                        case ORDER_UPDATED -> eventPublisher.publishOrderUpdatedEvent(eventPayload);
                        case AUTO_VALIDATE -> eventPublisher.publishAutoValidateEvent(eventPayload);
                        case PAYMENT_START -> eventPublisher.publishPaymentStartEvent(eventPayload);
                        case SHIPMENT_START -> eventPublisher.publishShipmentStartEvent(eventPayload);
                        case DELIVERED -> eventPublisher.publishDeliveredEvent(eventPayload);
                        case CANCEL -> eventPublisher.publishCancelEvent(eventPayload);
                        case RETRY_VALIDATION -> eventPublisher.publishRetryValidationEvent(eventPayload);
                        case RETRY_PAYMENT -> eventPublisher.publishRetryPaymentEvent(eventPayload);
                        case RETRY_SHIPMENT -> eventPublisher.publishRetryShipmentEvent(eventPayload);
                        default -> log.warn("No publisher for event: {}", eventType);
                    }
                    log.info("Published {} for order ID: {}", eventType.getEventType(), order.id());
                },
                () -> log.warn("Null or invalid order for event: {}", eventType.getEventType())
        );
    }

    /**
     * Extracts the order from the state context message headers.
     *
     * @param context the state machine context
     * @return an optional containing the order, or empty if not found or invalid
     */
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
            Message<OrderEventType> cancelMessage = MessageBuilder.withPayload(OrderEventType.AUTO_CANCEL)
                    .setHeader("order", context.getMessageHeader("order"))
                    .build();
            context.getStateMachine().sendEvent(Mono.just(cancelMessage)).subscribe();
            return false;
        };
    }

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
        return createRetryGuard("shipmentRetryCount");
    }

    @Bean
    public StateMachinePersister<OrderStatus, OrderEventType, String> persister() {
        return new DefaultStateMachinePersister<>(delegatePersister);
    }
}