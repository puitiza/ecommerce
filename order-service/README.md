# Order Service

The Order Service is a critical core business microservice responsible for managing the entire lifecycle of customer
orders within the e-commerce platform. It handles order creation, validation, state transitions, and integration with
other services like Payment and Shipment via a message broker (Kafka) and a REST client (Feign).

## Key Features

- **Order Lifecycle and State Management**: Manages orders through various states (e.g., PENDING, PROCESSING, SHIPPED,
  DELIVERED, CANCELLED) using a **state machine** to ensure a controlled flow.
- **Order Validation**: Ensures order integrity before processing.
- **Integration with Payment Service**: Initiates payment processing for new orders.
- **Integration with Product Service**: Interacts for product availability and inventory updates.
- **Integration with Shipment Service**: Triggers shipment creation upon successful payment.
- **Event-Driven Communication**: Utilizes Kafka for asynchronous communication, acting as both an event producer and
  consumer.
- **RESTful API**: Provides endpoints for order creation, retrieval, and updates.
- **Security**: Secured with JWT authentication via Keycloak.

-----

## Technologies

- **Spring Boot 3.0**: Framework for building robust microservices.
- **MySQL**: Relational database for persistent storage of order data.
- **Spring Data JPA / Hibernate**: For data persistence and ORM.
- **Apache Kafka**: For asynchronous, event-driven communication.
- **CloudEvents**: Used as the messaging format over Kafka for structured event data.
- **Spring Security**: For authentication and authorization using OAuth2 Resource Server.
- **Spring State Machine**: Manages the complex state transitions of an order.
- **Spring Cloud Config Client**: To fetch centralized configurations.
- **Eureka Client**: For service registration and discovery.
- **Springdoc OpenAPI**: For API documentation and Swagger UI.

-----

## Configuration

The Order Service retrieves its configuration from the Spring Cloud Config Server and uses Spring Boot's
autoconfiguration for JPA, OAuth2, and Kafka.

### Security Configuration

The service is secured with Keycloak OAuth2 and JWT. Spring Boot automatically configures a `JwtDecoder` using the
`issuer-uri` to validate tokens. Public endpoints (e.g., Swagger) are accessible without authentication.

### Kafka Configuration

The service is configured to both produce and consume events from a Kafka cluster. The configuration in
`application.yml` specifies the behavior of these clients.

#### 1\. Producer Configuration

The producer is responsible for sending messages to Kafka. Key configurations include:

- `key-serializer`: Defines how the message key is serialized. Using `StringSerializer` is common for simple string
  keys.
- `value-serializer`: Defines how the message value is serialized. The `CloudEventSerializer` is used to encode events
  according to the CloudEvents specification, ensuring a consistent and interoperable format.
- `properties`: Advanced settings like `retries` and `request.timeout.ms` are crucial for handling transient network
  failures and ensuring message delivery in a resilient manner.

#### 2\. Consumer Configuration

The consumer is responsible for reading messages from Kafka. Key configurations include:

- `group-id`: A unique identifier for the consumer group. All consumer instances within the same group work together to
  consume messages from a set of partitions, providing load balancing and high availability.
- `key-deserializer` and `value-deserializer`: These match the serializers used by the producer, ensuring the received
  messages are correctly decoded.
- `auto-offset-reset`: Determines what to do when a consumer starts for the first time or if there is no committed
  offset for its group. `latest` tells it to start consuming from the newest messages.
- `enable-auto-commit`: Set to `false` for manual offset management, giving the application explicit control over when
  an offset is considered processed and committed. This is a common practice for reliable message processing.
- `max-poll-records`: Limits the number of records returned in a single poll, helping to manage batch processing and
  resource usage.

### Dependencies

- `spring-boot-starter-web`: For REST APIs.
- `spring-boot-starter-data-jpa`: Auto-configures MySQL connectivity.
- `spring-boot-starter-security`: Enables OAuth2 resource server.
- `spring-boot-starter-oauth2-resource-server`: Configures JWT validation.
- `spring-kafka`: For event publishing to and consumption from Kafka.
- `spring-cloud-starter-config`: Connects to Config Server.
- `spring-cloud-starter-netflix-eureka-client`: Registers with Eureka.
- `spring-statemachine-core`: Core dependency for implementing the state machine.

-----

## Integration with Other Services

- **Config Server**: Retrieves configuration (e.g., database credentials, Keycloak URL) from `http://localhost:8885`.
- **Eureka Server**: Registers as `ORDER-SERVICE` for service discovery.
- **API Gateway**: Routes requests from `/orders/**` to this service.
- **Keycloak**: Validates JWT tokens for authenticated requests.
- **Kafka**: Publishes and consumes order events (e.g., `ORDER_CREATED`, `ORDER_CANCELLED`) to/from topics consumed by
  `payment-service`, `shipment-service`, and `notification-service`.
- **MySQL**: Stores order data with automatic schema updates (`ddl-auto: update`).

-----

## Local Setup

To run the Order Service locally:

1. Ensure [Config Server](../config-server/README.md), [Eureka Server](../service-registry/README.md),
   MySQL, and Kafka are running.
2. Navigate to the `order-service` directory.
3. Run the application: `./gradlew bootRun` or use your IDE.
4. Alternatively, use `docker-compose up -d order-service` from the root directory to start it as part of the overall
   microservices stack.

-----

## Testing

- **Unit Tests**: Implemented using JUnit 5 and Mockito for isolated component testing.
- **Integration Tests**: Utilizes Testcontainers for database and Kafka integration testing.
- Run tests with `./gradlew test`.

-----

## Production Considerations

- Ensure database credentials and Kafka broker URLs are securely managed using environment variables or a secrets
  management solution.
- Configure `spring.jpa.hibernate.ddl-auto` to `none` or `validate` in production for schema stability.
- Utilize distributed tracing (Azure Application Insights) and metrics (Azure Monitor) for production monitoring.
- Consider Kafka best practices for production, including replication factors, topic configurations, and consumer
  groups.

#### 1\. Performance: Load Balancer Cache

**Current State:** The application uses the default, simple cache for Spring Cloud LoadBalancer.
**Production Improvement:** For better performance and lower latency in service-to-service communication, it is highly
recommended to use **Caffeine Cache**.

**How to Implement:**

- Add the Caffeine dependency to your build file.
- Enable and configure Caffeine caching in your application.

#### 2\. Observability: Structured Logging

**Current State:** Logs are in a human-readable text format.
**Production Improvement:** To enable advanced analysis and monitoring, switch to a structured logging format like *
*JSON**. This makes logs easy for log aggregation platforms (e.g., ELK, Grafana Loki) to parse and index.

**How to Implement:**

- Add a logging library like `Logstash Logback Encoder`.
- Configure Logback to use a JSON encoder.

#### 3\. Observability: Log Level Management

**Current State:** Verbose `INFO` logs are shown for all dependencies (Kafka, Hibernate, Hikari).
**Production Improvement:** Reduce the verbosity of third-party libraries to prevent log clutter and focus on core
application events.

**How to Implement:**

- In your configuration file (`application.yml` or `application.properties`), adjust log levels for specific packages.

<!-- end list -->

```yaml
logging:
  level:
    org.apache.kafka: WARN
    org.hibernate: WARN
    com.zaxxer.hikari: WARN
```

-----

## Multi-Module Integration

The Order Service integrates with the `share-library` module for shared DTOs, exceptions, and utility classes, ensuring
consistency across the microservices' ecosystem.

-----

### Data Consistency and Transactional Integrity

To ensure the integrity and consistency of order data, all operations that involve multiple database actions or state
changes are executed within a transaction.

#### 1. API Endpoints

Endpoints such as `createOrder`, `updateOrder`, and `cancelOrder` are annotated with `@Transactional`. This guarantees
that if any part of the process fails (e.g., a database save or a state machine transition), the entire operation is
rolled back, preventing orders from being left in a corrupted or inconsistent state.

#### 2. Event-Driven Logic

The service consumes events from Kafka to update the state of an order. The processing of these events is also handled
within a transaction. A dedicated `OrderEventProcessor` class, separate from the Kafka listener, is responsible for this
logic.

- The `@KafkaListener` method deserializes the incoming event.
- It then delegates the processing to the `OrderEventProcessor`, which is annotated with `@Transactional`.

This design ensures that the state machine transition and the subsequent database update are atomic. If the database
save fails, the transaction is rolled back, and the Kafka message is not committed, allowing for safe retries and
preventing data loss or inconsistencies.

-----

## Future Enhancements and Best Practices

This section outlines potential improvements for better resilience, maintainability, and production readiness.

### Kafka Error Handling and Dead Letter Topics

Currently, logging is used to handle Kafka deserialization and processing errors. While simple, this approach has
limitations, as it lacks a **retry mechanism** and can lead to **message loss** or **duplicate processing** if not
carefully managed.

A more robust approach is to implement a centralized `CommonErrorHandler` provided by Spring Kafka. This allows for:

- **Automatic Retries**: Configure a backoff policy to automatically retry failed messages for transient errors.
- **Dead Letter Topic (DLT)**: After a configurable number of retries, a message can be automatically sent to a separate
  topic, known as a Dead Letter Topic. This prevents "poison pill" messages from blocking the consumer group and
  provides a place to store failed messages for manual inspection and reprocessing.
- **Centralized Logic**: A single class handles all consumer errors, reducing code duplication in individual listeners.

**Example of a `CommonErrorHandler` for Retries:**
The following code snippet shows how a `CommonErrorHandler` can be configured to retry failed messages up to three times
with a fixed 1-second delay.

```java

@Component("kafkaErrorHandler")
public class KafkaErrorHandler implements CommonErrorHandler {
    private final DefaultErrorHandler errorHandler;

    public KafkaErrorHandler() {
        // Retry a message 3 times with a 1-second delay
        this.errorHandler = new DefaultErrorHandler(new FixedBackOff(1000L, 3));
    }

    @Override
    public void handleRecord(Exception thrownException, ConsumerRecord<?, ?> record,
                             Consumer<?, ?> consumer, Message<?> message) {
        log.error("Error processing Kafka record on topic {}: {}", record.topic(), thrownException.getMessage());
        // Delegate to the default error handler, which will perform retries
        errorHandler.handleRecord(thrownException, record, consumer, message);
    }
}
```

To enable this, you would then configure your consumer's error handler in the listener with
`@KafkaListener(errorHandler = "kafkaErrorHandler")`.

-----

## Resources

- [Spring Cloud Config Documentation](https://cloud.spring.io/spring-cloud-config/)
- [Spring Boot Data JPA Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html)
- [Spring Security OAuth2 Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [CloudEvents Specification](https://cloudevents.io/)
- [Spring State Machine Reference](https://docs.spring.io/spring-statemachine/docs/current/reference)
- [Spring Kafka Error Handling Documentation](https://www.google.com/search?q=https://docs.spring.io/spring-kafka/reference/html/%23error-handling)