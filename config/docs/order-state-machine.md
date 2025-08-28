# Order State Machine

The `order-service` uses **Spring State Machine** to orchestrate the order lifecycle as a saga, handling validation,
payment, and shipping. This is an event-driven system: the `order-service` publishes events to a message broker (like
Kafka). Other services—such as product, payment, and shipment—listen, process the requests, and publish their results.
The `order-service` then listens for these responses and updates its state machine accordingly.

## States

| State                  | Description                                                                    |
|------------------------|--------------------------------------------------------------------------------|
| `CREATED`              | Initial state after an order is placed and basic validations are completed.    |
| `VALIDATION_PENDING`   | Awaiting stock validation and reservation from the product service.            |
| `VALIDATION_SUCCEEDED` | Stock has been successfully validated and reserved.                            |
| `VALIDATION_FAILED`    | Stock validation failed (e.g., insufficient inventory).                        |
| `PAYMENT_PENDING`      | Awaiting payment authorization from the payment service.                       |
| `PAYMENT_SUCCEEDED`    | Payment has been successfully authorized.                                      |
| `PAYMENT_FAILED`       | Payment authorization failed (e.g., card declined).                            |
| `SHIPPING_PENDING`     | Awaiting shipment processing from the shipment service.                        |
| `SHIPPING_SUCCEEDED`   | The order has been successfully shipped.                                       |
| `SHIPPING_FAILED`      | Shipment processing failed (e.g., logistics issues).                           |
| `FULFILLED`            | The order has been delivered and the lifecycle is complete.                    |
| `CANCELLED`            | The order has been canceled, either manually or due to an unrecoverable error. |

-----

## State Diagram

```mermaid
stateDiagram-v2
    [*] --> CREATED: Order Placed
    CREATED --> VALIDATION_PENDING: OrderCreated Event [Timeout 30s]
    CREATED --> CREATED: OrderUpdated Event
    state Validation {
        VALIDATION_PENDING --> VALIDATION_SUCCEEDED: ValidationSucceeded Event
        VALIDATION_PENDING --> VALIDATION_FAILED: ValidationFailed Event
        VALIDATION_FAILED --> VALIDATION_PENDING: RetryValidation Event [<=3 attempts]
        VALIDATION_FAILED --> CANCELLED: AutoCancel Event [After 3 failed attempts]
    }
    VALIDATION_SUCCEEDED --> PAYMENT_PENDING: PaymentStart Event
    state Payment {
        PAYMENT_PENDING --> PAYMENT_SUCCEEDED: PaymentSucceeded Event
        PAYMENT_PENDING --> PAYMENT_FAILED: PaymentFailed Event
        PAYMENT_FAILED --> PAYMENT_PENDING: RetryPayment Event [<=3 attempts]
        PAYMENT_FAILED --> CANCELLED: AutoCancel Event [After 3 failed attempts]
    }
    PAYMENT_SUCCEEDED --> SHIPPING_PENDING: ShipmentStart Event
    state Shipping {
        SHIPPING_PENDING --> SHIPPING_SUCCEEDED: ShipmentSucceeded Event
        SHIPPING_PENDING --> SHIPPING_FAILED: ShipmentFailed Event
        SHIPPING_FAILED --> SHIPPING_PENDING: RetryShipment Event [<=3 attempts]
        SHIPPING_FAILED --> CANCELLED: AutoCancel Event [After 3 failed attempts]
    }
    SHIPPING_SUCCEEDED --> FULFILLED: Delivered Event
    CREATED --> CANCELLED: Cancel Event
    VALIDATION_PENDING --> CANCELLED: Cancel Event [Restock]
    PAYMENT_PENDING --> CANCELLED: Cancel Event [Restock + Refund]
    SHIPPING_PENDING --> CANCELLED: Cancel Event [Restock + Refund + Reverse Shipment]
    [*] --> CANCELLED: Cancel Event
```

-----

## Transitions and Saga Logic

This table details the transitions, events, actions, and saga-related logic for each step in the order lifecycle.

| Source State           | Event                  | Target State           | Action(s)                                           | Retries    | Timeout | Compensating Action (on Cancel) |
|------------------------|------------------------|------------------------|-----------------------------------------------------|------------|---------|---------------------------------|
| `CREATED`              | `ORDER_CREATED`        | `VALIDATION_PENDING`   | Publish `OrderCreatedEvent` to `product-service`    | N/A        | 30s     | None                            |
| `CREATED`              | `ORDER_UPDATED`        | `CREATED`              | Publish `OrderUpdatedEvent` for notifications       | N/A        | N/A     | None                            |
| `VALIDATION_PENDING`   | `VALIDATION_SUCCEEDED` | `VALIDATION_SUCCEEDED` | Publish `PaymentStartEvent` to `payment-service`    | N/A        | 30s     | Restock                         |
| `VALIDATION_PENDING`   | `VALIDATION_FAILED`    | `VALIDATION_FAILED`    | Log failure, increment retry count                  | N/A        | N/A     | Restock                         |
| `VALIDATION_FAILED`    | `RETRY_VALIDATION`     | `VALIDATION_PENDING`   | Retry validation (if < 3 attempts)                  | 3 attempts | 30s     | Restock                         |
| `VALIDATION_FAILED`    | `AUTO_CANCEL`          | `CANCELLED`            | Publish `CancelEvent` (after 3 failed attempts)     | N/A        | N/A     | Restock                         |
| `VALIDATION_SUCCEEDED` | `PAYMENT_START`        | `PAYMENT_PENDING`      | Publish `PaymentStartEvent` to `payment-service`    | N/A        | 60s     | Restock                         |
| `PAYMENT_PENDING`      | `PAYMENT_SUCCEEDED`    | `PAYMENT_SUCCEEDED`    | Publish `ShipmentStartEvent` to `shipment-service`  | N/A        | N/A     | Restock + Refund                |
| `PAYMENT_PENDING`      | `PAYMENT_FAILED`       | `PAYMENT_FAILED`       | Log failure, increment retry count                  | N/A        | N/A     | Restock + Refund                |
| `PAYMENT_FAILED`       | `RETRY_PAYMENT`        | `PAYMENT_PENDING`      | Retry payment (if < 3 attempts)                     | 3 attempts | 60s     | Restock + Refund                |
| `PAYMENT_FAILED`       | `AUTO_CANCEL`          | `CANCELLED`            | Publish `CancelEvent` (after 3 failed attempts)     | N/A        | N/A     | Restock + Refund                |
| `PAYMENT_SUCCEEDED`    | `SHIPMENT_START`       | `SHIPPING_PENDING`     | Publish `ShipmentStartEvent` to `shipment-service`  | N/A        | 120s    | Restock + Refund                |
| `SHIPPING_PENDING`     | `SHIPMENT_SUCCEEDED`   | `SHIPPING_SUCCEEDED`   | Publish `DeliveredEvent`                            | N/A        | N/A     | Restock + Refund + Reverse      |
| `SHIPPING_PENDING`     | `SHIPMENT_FAILED`      | `SHIPPING_FAILED`      | Log failure, increment retry count                  | N/A        | N/A     | Restock + Refund + Reverse      |
| `SHIPPING_FAILED`      | `RETRY_SHIPMENT`       | `SHIPPING_PENDING`     | Retry shipment (if < 3 attempts)                    | 3 attempts | 120s    | Restock + Refund + Reverse      |
| `SHIPPING_FAILED`      | `AUTO_CANCEL`          | `CANCELLED`            | Publish `CancelEvent` (after 3 failed attempts)     | N/A        | N/A     | Restock + Refund + Reverse      |
| `SHIPPING_SUCCEEDED`   | `DELIVERED`            | `FULFILLED`            | Log fulfillment, end saga                           | N/A        | N/A     | None                            |
| Any `_PENDING`         | `CANCEL`               | `CANCELLED`            | Publish `CancelEvent`, trigger compensating actions | N/A        | N/A     | Per state                       |

-----

## Retry and Timeout Mechanisms

- **Retries:** All failed transitions (`VALIDATION_FAILED`, `PAYMENT_FAILED`, `SHIPPING_FAILED`) include a guard that
  allows up to three retry attempts. If the retry count is exceeded, the order is automatically moved to the `CANCELLED`
  state.
- **Timeouts:** The state machine incorporates timers for key transitions. If a service doesn't respond with a success
  or failure event within the specified time, the state machine automatically triggers a failure event and transitions
  to the corresponding `_FAILED` state.
    - **Validation Timeout:** 30 seconds
    - **Payment Timeout:** 60 seconds
    - **Shipment Timeout:** 120 seconds

## Error Handling

- **Logging:** All failure actions (`validationFailedAction`, `paymentFailedAction`, `shipmentFailedAction`) log
  detailed errors.
- **Compensation:** The `CANCELLED` state handles the "compensation" part of the saga pattern. When an order is
  canceled, an event is published that can trigger actions in other services, such as a **refund** (in the payment
  service) or a **restocking of inventory** (in the product service).

-----

## Integration with Other Services

The `order-service` acts as the orchestrator in this saga by communicating with other services through a message broker:

- **Product-Service:** Listens for `OrderCreatedEvent` and `RetryValidationEvent`. It responds with
  `ValidationSucceededEvent` or `ValidationFailedEvent`.
- **Payment-Service:** Listens for `PaymentStartEvent` and `RetryPaymentEvent`. It responds with `PaymentSucceededEvent`
  or `PaymentFailedEvent`.
- **Shipment-Service:** Listens for `ShipmentStartEvent` and `RetryShipmentEvent`. It responds with
  `ShipmentSucceededEvent` or `ShipmentFailedEvent`.

This event-driven architecture ensures loose coupling and high resilience, as each service can operate independently
while the `order-service` manages the overall flow.

-----

## Key Enhancements:

1. **Automatic Transition to VALIDATION_PENDING**: If an order remains in `CREATED` for 30 seconds without updates, it
   automatically transitions to `VALIDATION_PENDING` to start the validation process.
2. **Retry Limits**: After three failed attempts in `VALIDATION_FAILED`, `PAYMENT_FAILED`, or `SHIPPING_FAILED`, the
   order automatically transitions to `CANCELLED` via an `AutoCancel` event.
3. **Compensating Actions**:
    - `VALIDATION_PENDING` cancellation triggers a restock in `product-service`.
    - `PAYMENT_PENDING` cancellation triggers a restocking and a refund in `payment-service`.
    - `SHIPPING_PENDING` cancellation triggers a restocking, refund, and shipment reversal.
4. **Timeouts**: Each `_PENDING` state has a timeout to prevent orders from being stuck indefinitely.
5. **Endpoint for Confirmation**: An endpoint in `order-service` to explicitly start the validation process (optional,
   as the timeout handles automatic transitions).

-----

## Order Process Flow Diagrams

Below are the sequence diagrams for the key flows in an order's lifecycle.

1. **Create Order (Flow with 5-Minute Delay)**
   This is the happy path for when a customer creates an order and the system waits 5 minutes before sending it for
   validation.

    ```mermaid
    sequenceDiagram
        participant Client
        participant Order Service
        participant Kafka
        participant Product Service
        Client ->> Order Service: POST /api/orders (create order)
        activate Order Service
        Order Service ->> Order Service: Save Order (Status: CREATED)
        Order Service ->> Kafka: Publish 'AUTO_VALIDATE' event
        deactivate Order Service
        Note over Kafka: Event held in a retry topic<br/>for 5 minutes.
        Kafka -->> Order Service: Consume 'AUTO_VALIDATE' event
        activate Order Service
        Order Service ->> Order Service: Update Status to VALIDATION_PENDING
        Order Service ->> Kafka: Publish 'ORDER_CREATED' event
        deactivate Order Service
        Kafka -->> Product Service: Consume 'ORDER_CREATED' event
        activate Product Service
        Product Service ->> Product Service: Validate stock and products
        Product Service ->> Kafka: Publish 'VALIDATION_SUCCEEDED' event
        deactivate Product Service
        Kafka -->> Order Service: Consume 'VALIDATION_SUCCEEDED' event
        activate Order Service
        Order Service ->> Order Service: Update Status to VALIDATION_SUCCEEDED
        deactivate Order Service
    ``` 

2. **Update Order (Reset Timer)**
   This flow shows how updating an order's items resets the 5-minute timer.

    ```mermaid
    sequenceDiagram
        participant Client
        participant Order Service
        participant Kafka
        Note right of Client: The order already exists in CREATED state<br/>and an 'AUTO_VALIDATE' event is pending.
        Client ->> Order Service: PUT /api/orders/{id} (update items)
        activate Order Service
        Order Service ->> Order Service: Update order items and price
        Note over Order Service, Kafka: A new 'AUTO_VALIDATE' event is published.<br/>Kafka will handle the latest event,<br/>effectively resetting the timer.
        Order Service ->> Kafka: Publish new 'AUTO_VALIDATE' event
        deactivate Order Service
    ```

3. **Confirm Order (Immediate Submission)**
   This is the happy path for when a customer decides not to wait 5 minutes and confirms the order for immediate
   validation.

    ```mermaid
    sequenceDiagram
        participant Client
        participant Order Service
        participant Kafka
        participant Product Service
        Note right of Client: The order already exists in CREATED state.
        Client ->> Order Service: POST /api/orders/{id}/confirm
        activate Order Service
        Order Service ->> Order Service: Update Status to VALIDATION_PENDING
        Note over Order Service, Kafka: The 5-minute delay is skipped.
        Order Service ->> Kafka: Publish 'ORDER_CREATED' event (immediately)
        deactivate Order Service
        Kafka -->> Product Service: Consume 'ORDER_CREATED' event
        activate Product Service
        Product Service ->> Product Service: Validate stock and products
        Product Service ->> Kafka: Publish 'VALIDATION_SUCCEEDED' event
        deactivate Product Service
        Kafka -->> Order Service: Consume 'VALIDATION_SUCCEEDED' event
        activate Order Service
        Order Service ->> Order Service: Update Status to VALIDATION_SUCCEEDED
        deactivate Order Service
    ```

4. **Validation Failure with Retries and Cancellation (Unhappy Path)**
   This diagram shows the flow when product validation fails, the system retries 3 times, and, upon final failure,
   automatically cancels the order.

    ```mermaid
    sequenceDiagram
        participant Product Service
        participant Kafka
        participant Order Service
        participant DLT Listener
        Product Service ->> Kafka: Publish 'VALIDATION_FAILED'
        Kafka -->> Order Service: Consume 'VALIDATION_FAILED'
        activate Order Service
        Order Service ->> Order Service: Update Status to VALIDATION_FAILED
        Order Service ->> Kafka: Publish 'RETRY_VALIDATION'
        deactivate Order Service
    
        loop 3 Retries
            Note over Kafka: Event held in a retry topic<br/>for 30 seconds.
            Kafka -->> Order Service: Consume 'RETRY_VALIDATION'
            activate Order Service
            Order Service ->> Order Service: Update Status to VALIDATION_PENDING
            Order Service ->> Kafka: Publish 'ORDER_CREATED' (to retry)
            deactivate Order Service
            Kafka -->> Product Service: Consume 'ORDER_CREATED'
            Product Service ->> Kafka: Publish 'VALIDATION_FAILED'
        end
    
        Note over Kafka: After 3 failures, the message<br/>is sent to the Dead Letter Topic (DLT).
        Kafka -->> DLT Listener: Consume failed event from DLT
        activate DLT Listener
        DLT Listener ->> Order Service: Initiate order cancellation
        activate Order Service
        Order Service ->> Order Service: Update Status to CANCELLED
        Order Service ->> Kafka: Publish 'ORDER_CANCELLED' event
        deactivate Order Service
        deactivate DLT Listener
    ```

5. **Automatic Cancellation for Non-Payment (Timeout)**
   This flow describes how an order is automatically cancelled if the user does not complete payment within a specified
   time (e.g., 30 minutes).

    ```mermaid
    sequenceDiagram
        participant Order Service
        participant Kafka
        Note right of Order Service: Order is in PAYMENT_PENDING state.
        Order Service ->> Kafka: Publish 'CHECK_PAYMENT_TIMEOUT' event
        Note over Kafka: Event held in a retry topic<br/>for 30 minutes.
        Kafka -->> Order Service: Consume 'CHECK_PAYMENT_TIMEOUT' event
        activate Order Service
        Order Service ->> Order Service: Check the order's current state
    
        alt If status is still PAYMENT_PENDING
            Order Service ->> Order Service: Update Status to CANCELLED
            Order Service ->> Kafka: Publish 'ORDER_CANCELLED' event (reason: timeout)
        else Payment was already processed
            Order Service ->> Order Service: Do nothing
        end
        deactivate Order Service
    ```

6. **Complete Order Lifecycle (End-to-End Happy Path)**
   This diagram shows the full journey of a successful order, from creation to final delivery.

    ```mermaid
    sequenceDiagram
        participant Client
        participant Order Service
        participant Kafka
        participant Product Service
        participant Payment Service
        participant Shipping Service
        Client ->> Order Service: 1. Create Order
        Order Service ->> Kafka: 2. Publish 'AUTO_VALIDATE' (with 5 min delay)
        Kafka -->> Order Service: 3. Consume 'AUTO_VALIDATE'
        Order Service ->> Kafka: 4. Publish 'ORDER_CREATED'
        Kafka -->> Product Service: 5. Validate Products
        Product Service ->> Kafka: 6. Publish 'VALIDATION_SUCCEEDED'
        Kafka -->> Order Service: 7. Consume 'VALIDATION_SUCCEEDED'
        Order Service ->> Kafka: 8. Publish 'PAYMENT_START'
        Kafka -->> Payment Service: 9. Process Payment
        Payment Service ->> Kafka: 10. Publish 'PAYMENT_SUCCEEDED'
        Kafka -->> Order Service: 11. Consume 'PAYMENT_SUCCEEDED'
        Order Service ->> Kafka: 12. Publish 'SHIPMENT_START'
        Kafka -->> Shipping Service: 13. Prepare Shipment
        Shipping Service ->> Kafka: 14. Publish 'SHIPMENT_SUCCEEDED'
        Kafka -->> Order Service: 15. Consume 'SHIPMENT_SUCCEEDED'
        Order Service ->> Order Service: 16. Update Status to FULFILLED
    ```