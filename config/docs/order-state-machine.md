# Order State Machine

The `order-service` uses **Spring State Machine** to manage the order lifecycle as a saga, orchestrating validation,
payment, and shipping. It operates as an event-driven system, publishing events to a message broker (**Kafka** for main
events, **RabbitMQ** for delayed validation). Services like `product-service`, `payment-service`, and `shipment-service`
consume these events, process requests, and publish responses, which the `order-service` uses to update the state
machine.

## States

| State                  | Description                                                                                       |
|------------------------|---------------------------------------------------------------------------------------------------|
| `CREATED`              | Order is created and awaiting validation trigger (manual or delayed).                             |
| `VALIDATION_PENDING`   | Awaiting stock validation and reservation from `product-service`.                                 |
| `VALIDATION_SUCCEEDED` | Stock validated and reserved successfully.                                                        |
| `VALIDATION_FAILED`    | Stock validation failed (e.g., insufficient inventory).                                           |
| `PAYMENT_PENDING`      | Awaiting payment authorization from `payment-service`.                                            |
| `PAYMENT_SUCCEEDED`    | Payment authorized successfully.                                                                  |
| `PAYMENT_FAILED`       | Payment authorization failed (e.g., card declined).                                               |
| `SHIPPING_PENDING`     | Awaiting shipment processing from `shipment-service`.                                             |
| `SHIPPING_SUCCEEDED`   | Order shipped successfully.                                                                       |
| `SHIPPING_FAILED`      | Shipment processing failed (e.g., logistics issues).                                              |
| `FULFILLED`            | Order delivered, completing the lifecycle.                                                        |
| `CANCELLED`            | Order cancelled manually or due to unrecoverable errors (e.g., validation failure after retries). |

## State Diagram

```mermaid
stateDiagram-v2
    [*] --> CREATED: Order Created
    CREATED --> VALIDATION_PENDING: AutoValidate Event [After 30m delay]
    CREATED --> VALIDATION_PENDING: OrderConfirmed Event
    CREATED --> CREATED: OrderUpdated Event
    state Validation {
        VALIDATION_PENDING --> VALIDATION_SUCCEEDED: ValidationSucceeded Event
        VALIDATION_PENDING --> VALIDATION_FAILED: ValidationFailed Event
        VALIDATION_FAILED --> VALIDATION_PENDING: RetryValidation Event [<=3 attempts, 5m delay]
        VALIDATION_FAILED --> CANCELLED: AutoCancel Event [After 3 failed attempts]
    }
    VALIDATION_SUCCEEDED --> PAYMENT_PENDING: PaymentStart Event
    state Payment {
        PAYMENT_PENDING --> PAYMENT_SUCCEEDED: PaymentSucceeded Event
        PAYMENT_PENDING --> PAYMENT_FAILED: PaymentFailed Event
        PAYMENT_FAILED --> PAYMENT_PENDING: RetryPayment Event [<=3 attempts, 2m delay]
        PAYMENT_FAILED --> CANCELLED: AutoCancel Event [After 3 failed attempts]
    }
    PAYMENT_SUCCEEDED --> SHIPPING_PENDING: ShipmentStart Event
    state Shipping {
        SHIPPING_PENDING --> SHIPPING_SUCCEEDED: ShipmentSucceeded Event
        SHIPPING_PENDING --> SHIPPING_FAILED: ShipmentFailed Event
        SHIPPING_FAILED --> SHIPPING_PENDING: RetryShipment Event [<=3 attempts, 1m delay]
        SHIPPING_FAILED --> CANCELLED: AutoCancel Event [After 3 failed attempts]
    }
    SHIPPING_SUCCEEDED --> FULFILLED: Delivered Event
    CREATED --> CANCELLED: Cancel Event
    VALIDATION_PENDING --> CANCELLED: Cancel Event [Restock]
    PAYMENT_PENDING --> CANCELLED: Cancel Event [Restock + Refund]
    SHIPPING_PENDING --> CANCELLED: Cancel Event [Restock + Refund + Reverse Shipment]
```

## Transitions and Saga Logic

| Source State           | Event                  | Target State           | Action(s)                                           | Retries    | Timeout/Delay | Compensating Action (on Cancel) |
|------------------------|------------------------|------------------------|-----------------------------------------------------|------------|---------------|---------------------------------|
| `CREATED`              | `AUTO_VALIDATE`        | `VALIDATION_PENDING`   | Consume RabbitMQ delayed `OrderCreatedEvent`        | N/A        | 30m delay     | None                            |
| `CREATED`              | `ORDER_CONFIRMED`      | `VALIDATION_PENDING`   | Publish `OrderCreatedEvent` to `product-service`    | N/A        | N/A           | None                            |
| `CREATED`              | `ORDER_UPDATED`        | `CREATED`              | Reset RabbitMQ delay timer, update order            | N/A        | 30m reset     | None                            |
| `VALIDATION_PENDING`   | `VALIDATION_SUCCEEDED` | `VALIDATION_SUCCEEDED` | Publish `PaymentStartEvent` to `payment-service`    | N/A        | 30s timeout   | Restock                         |
| `VALIDATION_PENDING`   | `VALIDATION_FAILED`    | `VALIDATION_FAILED`    | Log failure, increment retry count                  | N/A        | N/A           | Restock                         |
| `VALIDATION_FAILED`    | `RETRY_VALIDATION`     | `VALIDATION_PENDING`   | Publish delayed `OrderCreatedEvent`                 | 3 attempts | 5m delay      | Restock                         |
| `VALIDATION_FAILED`    | `AUTO_CANCEL`          | `CANCELLED`            | Publish `AutoCancelEvent` (after 3 failed attempts) | N/A        | N/A           | Restock                         |
| `VALIDATION_SUCCEEDED` | `PAYMENT_START`        | `PAYMENT_PENDING`      | Publish `PaymentStartEvent` to `payment-service`    | N/A        | 2m delay      | Restock                         |
| `PAYMENT_PENDING`      | `PAYMENT_SUCCEEDED`    | `PAYMENT_SUCCEEDED`    | Publish `ShipmentStartEvent` to `shipment-service`  | N/A        | 60s timeout   | Restock + Refund                |
| `PAYMENT_PENDING`      | `PAYMENT_FAILED`       | `PAYMENT_FAILED`       | Log failure, increment retry count                  | N/A        | N/A           | Restock + Refund                |
| `PAYMENT_FAILED`       | `RETRY_PAYMENT`        | `PAYMENT_PENDING`      | Publish delayed `PaymentStartEvent`                 | 3 attempts | 2m delay      | Restock + Refund                |
| `PAYMENT_FAILED`       | `AUTO_CANCEL`          | `CANCELLED`            | Publish `AutoCancelEvent` (after 3 failed attempts) | N/A        | N/A           | Restock + Refund                |
| `PAYMENT_SUCCEEDED`    | `SHIPMENT_START`       | `SHIPPING_PENDING`     | Publish `ShipmentStartEvent` to `shipment-service`  | N/A        | 1m delay      | Restock + Refund                |
| `SHIPPING_PENDING`     | `SHIPPING_SUCCEEDED`   | `SHIPPING_SUCCEEDED`   | Publish `DeliveredEvent`                            | N/A        | 120s timeout  | Restock + Refund + Reverse      |
| `SHIPPING_PENDING`     | `SHIPPING_FAILED`      | `SHIPPING_FAILED`      | Log failure, increment retry count                  | N/A        | N/A           | Restock + Refund + Reverse      |
| `SHIPPING_FAILED`      | `RETRY_SHIPMENT`       | `SHIPPING_PENDING`     | Publish delayed `ShipmentStartEvent`                | 3 attempts | 1m delay      | Restock + Refund + Reverse      |
| `SHIPPING_FAILED`      | `AUTO_CANCEL`          | `CANCELLED`            | Publish `AutoCancelEvent` (after 3 failed attempts) | N/A        | N/A           | Restock + Refund + Reverse      |
| `SHIPPING_SUCCEEDED`   | `DELIVERED`            | `FULFILLED`            | Log fulfillment, end saga                           | N/A        | N/A           | None                            |
| Any `_PENDING`         | `CANCEL`               | `CANCELLED`            | Publish `CancelEvent`, trigger compensating actions | N/A        | N/A           | Per state                       |

## Retry and Timeout Mechanisms

- **Retries**:
    - Validation, payment, and shipment failures allow up to 3 retry attempts with delays:
        - Validation: 5-minute delay between retries (via RabbitMQ).
        - Payment: 2-minute delay between retries (via RabbitMQ).
        - Shipment: 1-minute delay between retries (via RabbitMQ).
    - After 3 failed attempts, the order transitions to `CANCELLED` via an `AutoCancelEvent`.
- **Timeouts**:
    - `VALIDATION_PENDING`: 30 seconds for service response.
    - `PAYMENT_PENDING`: 60 seconds for service response.
    - `SHIPPING_PENDING`: 120 seconds for service response.
    - If no response is received, the state transitions to the corresponding `_FAILED` state.
- **RabbitMQ Delay**:
    - Orders in `CREATED` state are sent to a RabbitMQ delay exchange with a **30-minute delay** before transitioning to
      `VALIDATION_PENDING`.
    - Updates to the order reset this 30-minute timer.
    - Validation retries use a 5-minute delay to avoid overwhelming the system.

## Error Handling

- **Logging**: Failures in validation, payment, or shipment are logged with detailed error messages.
- **Compensation**: The `CANCELLED` state triggers compensating actions:
    - `VALIDATION_PENDING`: Restock inventory in `product-service`.
    - `PAYMENT_PENDING`: Restock + refund in `payment-service`.
    - `SHIPPING_PENDING`: Restock + refund + reverse shipment in `shipment-service`.
- **Dead Letter Queue (DLQ)**: Kafka’s DLQ captures unprocessed events after retries, triggering cancellation.

## Integration with Other Services

- **Message Brokers**:
    - **RabbitMQ**: Handles delayed events (30m for initial validation, 5m for validation retries, 2m for payment
      retries, 1m for shipment retries).
    - **Kafka**: Manages main event flow and retries, with a DLQ for unprocessed events.
- **Services**:
    - `product-service`: Consumes `OrderCreatedEvent`, responds with `ValidationSucceeded` or `ValidationFailed`.
    - `payment-service`: Consumes `PaymentStartEvent`, responds with `PaymentSucceeded` or `PaymentFailed`.
    - `shipment-service`: Consumes `ShipmentStartEvent`, responds with `ShipmentSucceeded` or `ShipmentFailed`.

## Key Features

1. **Delayed Validation**: Orders in `CREATED` state wait **30 minutes** (via RabbitMQ delay exchange) before
   transitioning to `VALIDATION_PENDING`, allowing users time to update the order, unless manually confirmed.
2. **Retry Logic**: Up to 3 retry attempts for validation (5m delay), payment (2m delay), and shipment (1m delay).
   Failure after 3 attempts triggers `CANCELLED`.
3. **Compensating Actions**: Cancelling an order triggers appropriate actions (restock, refund, shipment reversal) based
   on the state.
4. **Timeouts**: Prevent orders from stalling in `_PENDING` states.
5. **Order Confirmation Endpoint**: Allows immediate validation, bypassing the 30-minute delay.

## Sequence Diagrams

### 1. Order Creation with Delayed Validation

```mermaid
sequenceDiagram
    participant Client
    participant OrderService
    participant DB as Order DB
    participant Rabbit as RabbitMQ Delay Exchange
    participant Kafka
    participant ProductService
    Client ->> OrderService: POST /api/orders
    OrderService ->> DB: Save Order (status=CREATED, retryCountValidation=0)
    OrderService ->> Rabbit: Publish "order_created" (delay=30m)
    Note right of Rabbit: Delay 30 minutes
    Rabbit ->> OrderService: Consume "order_created"
    OrderService ->> DB: Update (status=VALIDATION_PENDING)
    OrderService ->> Kafka: Publish "order.created"
    Kafka ->> ProductService: Consume "order.created"
    ProductService ->> Kafka: Publish "validation.succeeded" or "validation.failed"
    Kafka ->> OrderService: Consume response
    alt Validation Succeeded
        OrderService ->> DB: Update (status=VALIDATION_SUCCEEDED)
        OrderService ->> Kafka: Publish "payment.start"
    else Validation Failed
        OrderService ->> DB: Increment retryCountValidation
        alt retryCountValidation < 3
            OrderService ->> Rabbit: Re-publish "order_created" (delay=5m)
        else retryCountValidation >= 3
            OrderService ->> DB: Update (status=CANCELLED)
            OrderService ->> Kafka: Publish "order.auto_cancel"
        end
    end
```

### 2. Order Update (Reset Delay)

```mermaid
sequenceDiagram
    participant Client
    participant OrderService
    participant DB as Order DB
    participant Rabbit as RabbitMQ Delay Exchange
    Note right of Client: Order in CREATED state with pending delay
    Client ->> OrderService: PUT /api/orders/{id}
    OrderService ->> DB: Update order items/price
    OrderService ->> Rabbit: Publish new "order_created" (delay=30m)
    Note right of Rabbit: Replaces previous delayed event, resets 30m timer
```

### 3. Immediate Order Confirmation

```mermaid
sequenceDiagram
    participant Client
    participant OrderService
    participant DB as Order DB
    participant Kafka
    participant ProductService
    Client ->> OrderService: POST /api/orders/{id}/confirm
    OrderService ->> DB: Update (status=VALIDATION_PENDING)
    OrderService ->> Kafka: Publish "order.created"
    Kafka ->> ProductService: Consume "order.created"
    ProductService ->> Kafka: Publish "validation.succeeded" or "validation.failed"
    Kafka ->> OrderService: Consume response
    alt Validation Succeeded
        OrderService ->> DB: Update (status=VALIDATION_SUCCEEDED)
        OrderService ->> Kafka: Publish "payment.start"
    else Validation Failed
        OrderService ->> DB: Increment retryCountValidation
        alt retryCountValidation < 3
            OrderService ->> Rabbit: Publish "order_created" (delay=5m)
        else retryCountValidation >= 3
            OrderService ->> DB: Update (status=CANCELLED)
            OrderService ->> Kafka: Publish "order.auto_cancel"
        end
    end
```

### 4. Validation Failure with Retries and Cancellation

```mermaid
sequenceDiagram
    participant ProductService
    participant Kafka
    participant OrderService
    participant DB as Order DB
    participant Rabbit as RabbitMQ Delay Exchange
    participant DLT as Kafka DLQ
    ProductService ->> Kafka: Publish "validation.failed"
    Kafka ->> OrderService: Consume "validation.failed"
    OrderService ->> DB: Update (status=VALIDATION_FAILED, retryCountValidation+1)

    loop 3 Retries
        alt retryCountValidation < 3
            OrderService ->> Rabbit: Publish "order_created" (delay=5m)
            Note right of Rabbit: Delay 5 minutes
            Rabbit ->> OrderService: Consume "order_created"
            OrderService ->> DB: Update (status=VALIDATION_PENDING)
            OrderService ->> Kafka: Publish "order.created"
            Kafka ->> ProductService: Consume "order.created"
            ProductService ->> Kafka: Publish "validation.failed"
            Kafka ->> OrderService: Consume "validation.failed"
            OrderService ->> DB: Increment retryCountValidation
        end
    end

    alt retryCountValidation >= 3
        OrderService ->> DB: Update (status=CANCELLED)
        OrderService ->> Kafka: Publish "order.auto_cancel"
        Kafka ->> DLT: Move to DLQ if unprocessed
    end
```

### 5. Automatic Cancellation for Non-Payment (Timeout)

```mermaid
sequenceDiagram
    participant OrderService
    participant DB as Order DB
    participant Kafka
    participant PaymentService
    Note right of OrderService: Order in PAYMENT_PENDING state
    OrderService ->> Kafka: Publish "check_payment_timeout"
    Note right of Kafka: Timeout after 60s
    Kafka ->> OrderService: Consume "check_payment_timeout"
    OrderService ->> DB: Check order status
    alt Status is PAYMENT_PENDING
        OrderService ->> DB: Update (status=CANCELLED)
        OrderService ->> Kafka: Publish "order.auto_cancel" (reason: timeout)
    else Payment processed
        OrderService ->> OrderService: Do nothing
    end
```

### 6. Full Order Lifecycle (Happy Path)

```mermaid
sequenceDiagram
    participant Client
    participant OrderService
    participant DB as Order DB
    participant Rabbit as RabbitMQ Delay Exchange
    participant Kafka
    participant ProductService
    participant PaymentService
    participant ShipmentService
    Client ->> OrderService: POST /api/orders
    OrderService ->> DB: Save (status=CREATED, retryCountValidation=0)
    OrderService ->> Rabbit: Publish "order_created" (delay=30m)
    Rabbit ->> OrderService: Consume "order_created"
    OrderService ->> DB: Update (status=VALIDATION_PENDING)
    OrderService ->> Kafka: Publish "order.created"
    Kafka ->> ProductService: Consume and validate
    ProductService ->> Kafka: Publish "validation.succeeded"
    Kafka ->> OrderService: Update (status=VALIDATION_SUCCEEDED)
    OrderService ->> Kafka: Publish "payment.start"
    OrderService ->> DB: Save (status=PAYMENT_PENDING, retryCountPayment=0)
    OrderService ->> Rabbit: Publish "payment.start" (delay=2m)
    Rabbit ->> OrderService: Consume "payment.start"
    OrderService ->> Kafka: Publish "payment.start"
    Kafka ->> PaymentService: Consume and process payment
    PaymentService ->> Kafka: Publish "payment.succeeded"
    Kafka ->> OrderService: Update (status=PAYMENT_SUCCEEDED)
    OrderService ->> Kafka: Publish "shipment.start"
    OrderService ->> DB: Save (status=SHIPPING_PENDING, retryCountShipment=0)
    OrderService ->> Rabbit: Publish "shipment.start" (delay=1m)
    Rabbit ->> OrderService: Consume "shipment.start"
    OrderService ->> Kafka: Publish "shipment.start"
    Kafka ->> ShipmentService: Consume and process shipment
    ShipmentService ->> Kafka: Publish "shipment.succeeded"
    Kafka ->> OrderService: Update (status=SHIPPING_SUCCEEDED)
    OrderService ->> Kafka: Publish "order.delivered"
    ShipmentService ->> Kafka: Publish "order.delivered"
    Kafka ->> OrderService: Update (status=FULFILLED)
```