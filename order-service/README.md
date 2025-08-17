# Order Service

The **Order Service** is a core microservice in the e-commerce platform, responsible for managing the lifecycle of
customer orders. It handles order creation, validation, state transitions, and integration with other services (e.g.,
Payment, Shipment, Product) using asynchronous messaging (Kafka) and synchronous REST calls (Feign). The service is
designed for reliability, scalability, and maintainability, leveraging a state machine for controlled order state
transitions and robust error handling for production readiness.

---

## Table of Contents

1. [Key Features](#key-features)
2. [Architecture](#architecture)
    - [Technologies](#technologies)
    - [Integration with Other Services](#integration-with-other-services)
    - [Data Consistency and Transactional Integrity](#data-consistency-and-transactional-integrity)
3. [Configuration](#configuration)
    - [Kafka Configuration](#kafka-configuration)
    - [Security Configuration](#security-configuration)
4. [Setup and Deployment](#setup-and-deployment)
    - [Local Setup](#local-setup)
    - [Production Considerations](#production-considerations)
    - [Monitoring and Actuator Endpoints](#monitoring-and-actuator-endpoints)
5. [Error Handling and Resiliency](#error-handling-and-resiliency)
    - [Business Exceptions](#business-exceptions)
    - [Infrastructure Exceptions](#infrastructure-exceptions)
    - [Kafka Error Handling](#kafka-error-handling)
    - [API Resiliency](#api-resiliency)
6. [Testing](#testing)
7. [Future Enhancements](#future-enhancements)
    - [Performance Improvements](#performance-improvements)
    - [Observability Enhancements](#observability-enhancements)
    - [Kafka Enhancements](#kafka-enhancements)
    - [Security Enhancements](#security-enhancements)
8. [Resources](#resources)

---

## Key Features

- **Order Lifecycle Management**: Manages orders through states (e.g., PENDING, PROCESSING, SHIPPED, DELIVERED,
  CANCELLED) using a Spring State Machine for controlled transitions.
- **Order Validation**: Ensures order integrity (e.g., product availability, valid quantities) before processing.
- **Service Integration**:
    - **Payment Service**: Initiates payment processing for new orders.
    - **Product Service**: Validates product availability and updates inventory.
    - **Shipment Service**: Triggers shipment creation upon successful payment.
- **Event-Driven Communication**: Publishes and consumes events via Kafka using the CloudEvents format for
  interoperability.
- **RESTful API**: Exposes endpoints for order creation, retrieval, and updates.
- **Security**: Implements JWT-based authentication and authorization via Keycloak with dynamic URL permissions.
- **Resiliency**: Handles errors gracefully with retries, Dead Letter Topics (DLTs), and robust exception handling.
- **Scalability**: Supports load balancing and service discovery via Eureka.
- **Monitoring**: Exposes Actuator endpoints for health checks and diagnostics.

---

## Architecture

### Technologies

The Order Service is built with modern frameworks and tools for reliability and maintainability:

- **Spring Boot 3.0**: Core framework for microservice development.
- **MySQL**: Relational database for persistent order storage.
- **Spring Data JPA / Hibernate**: Simplifies data persistence and ORM.
- **Apache Kafka**: Enables asynchronous, event-driven communication.
- **CloudEvents**: Standardizes event messaging over Kafka.
- **Spring Security**: Secures the service with OAuth2 and JWT.
- **Spring State Machine**: Manages complex order state transitions.
- **Spring Cloud Config Client**: Fetches centralized configurations.
- **Spring Cloud Netflix Eureka**: Handles service registration and discovery.
- **Springdoc OpenAPI**: Generates API documentation and Swagger UI.
- **Spring Boot Actuator**: Provides monitoring and management endpoints.

### Integration with Other Services

The Order Service integrates with the following components:

- **Spring Cloud Config Server**: Fetches configuration (e.g., database credentials, Keycloak URL) from
  `http://localhost:8885`.
- **Eureka Server**: Registers as `ORDER-SERVICE` for service discovery.
- **API Gateway**: Routes requests from `/orders/**` to the service.
- **Keycloak**: Validates JWT tokens for authenticated requests.
- **Kafka**: Publishes and consumes events (e.g., `ORDER_CREATED`, `ORDER_CANCELLED`) to/from topics used by
  `payment-service`, `shipment-service`, and `notification-service`.
- **MySQL**: Stores order data with automatic schema updates (`ddl-auto: update`).
- **Shared Library**: Uses the `share-library` module for consistent DTOs, exceptions, and utilities across
  microservices.

### Data Consistency and Transactional Integrity

To ensure data integrity:

- **API Endpoints**: Operations like `createOrder`, `updateOrder`, and `cancelOrder` are annotated with `@Transactional`
  to ensure atomicity. If any step (e.g., database save, state transition) fails, the transaction rolls back, preventing
  inconsistent states.
- **Event-Driven Logic**: Kafka event processing is transactional. The `OrderEventProcessor` class, annotated with
  `@Transactional`, handles state transitions and database updates atomically. If an update fails, the Kafka offset is
  not committed, allowing retries.

---

## Configuration

### Kafka Configuration

The service acts as both a Kafka producer and consumer, configured in `application.yml`:

#### Producer Configuration

- **Purpose**: Sends events (e.g., `ORDER_CREATED`) to Kafka topics.
- **Key Settings**:
    - `key-serializer`: `StringSerializer` for string keys.
    - `value-serializer`: `CloudEventSerializer` for CloudEvents-compliant messages.
    - `properties`:
        - `max.request.size: 1000000`: Limits message size to 1MB.
        - `retry.backoff.ms: 1000`: 1-second delay between retries.
        - `retries: 10`: Attempts up to 10 retries for transient failures.
        - `linger.ms: 0`: Sends messages immediately for low latency.
        - `request.timeout.ms: 30000`: 30-second timeout for broker responses.

#### Consumer Configuration

- **Purpose**: Consumes events (e.g., `PAYMENT_SUCCEEDED`, `SHIPMENT_FAILED`) to update order states.
- **Key Settings**:
    - `group-id`: `order-service-group` for load balancing across consumer instances.
    - `key-deserializer`: `ErrorHandlingDeserializer` with `StringDeserializer` delegate.
    - `value-deserializer`: `ErrorHandlingDeserializer` with `CloudEventDeserializer` delegate.
    - `auto-offset-reset: latest`: Starts consuming from the latest messages.
    - `enable-auto-commit: false`: Enables manual offset commits for reliability.
    - `max-poll-records: 100`: Limits records per poll for controlled processing.
    - `fetch-min-size: 1`, `fetch-max-wait: 500`: Optimizes for low latency.

```yaml
spring:
  kafka:
    client-id: order-service
    bootstrap-servers: ${KAFKA_SERVER_URL:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: io.cloudevents.kafka.CloudEventSerializer
      properties:
        max.request.size: 1000000
        metadata.max.idle.ms: 180000
        request.timeout.ms: 30000
        linger.ms: 0
        retry.backoff.ms: 1000
        retries: 10
    consumer:
      group-id: order-service-group
      key-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
      properties:
        spring.deserializer.key.delegate.class: org.apache.kafka.common.serialization.StringDeserializer
        spring.deserializer.value.delegate.class: io.cloudevents.kafka.CloudEventDeserializer
        metadata.max.age.ms: 180000
        connections.max.idle.ms: 180000
      auto-offset-reset: latest
      enable-auto-commit: false
      max-poll-records: 100
      fetch-min-size: 1
      fetch-max-wait: 500
```

### Security Configuration

- **Authentication**: Uses Keycloak with OAuth2 and JWT. Spring Security’s `JwtDecoder` validates tokens via the
  `issuer-uri` from `application.yml`.
- **Authorization**: Only authenticated requests access protected endpoints. Public endpoints (e.g., Swagger UI,
  Actuator) are accessible without authentication, configured dynamically via `security.permit-urls`.
- **Configuration**:
  ```yaml
  security:
    permit-urls:
      swagger:
        - /orders/v3/api-docs/**
        - /orders/swagger-ui/**
        - /orders/swagger-ui.html
        - /favicon.ico
      actuator:
        - /actuator/health/**
        - /actuator/info/**
        - /actuator/env/**
        - /actuator/configprops/**
  spring:
    security:
      oauth2:
        resourceserver:
          jwt:
            issuer-uri: ${keycloak.realm.url}
  ```

- **Purpose**:
    - Dynamically configures permitted URLs for `GET` requests (e.g., Swagger, Actuator) via `application.yml`.
    - Supports adding new URL categories (e.g., `public`, `metrics`) without code changes.
    - Managed via Spring Cloud Config Server for centralized updates.
    - Extensible to support specific HTTP methods or role-based access in the future.

---

## Setup and Deployment

### Local Setup

To run the Order Service locally:

1. **Prerequisites**:
    - Running services: [Config Server](../config-server/README.md), [Eureka Server](../service-registry/README.md),
      MySQL, Kafka.
    - Kafka topics: Ensure topics like `order_created`, `payment_succeeded`, etc., are created.
    - MySQL database: Create `order_db` with credentials `order_user`/`order_password`.

2. **Steps**:
    - Navigate to the `order-service` directory.
    - Run: `./gradlew bootRun` or use your IDE.
    - Alternatively, use `docker-compose up -d order-service` from the root directory to start the service as part of
      the microservices stack.

3. **Verification**:
    - Access Swagger UI at `http://localhost:8080/orders/swagger-ui.html` or
      `http://localhost:8090/orders/swagger-ui.html` (via API Gateway).
    - Verify service registration in Eureka at `http://localhost:8761`.
    - Monitor Kafka topics for published events.
    - Test Actuator endpoints (e.g., `http://localhost:8080/actuator/health`).

### Production Considerations

- **Security**:
    - Store sensitive data (e.g., database credentials, Kafka URLs, Keycloak secrets) in environment variables or a
      secrets manager (e.g., AWS Secrets Manager, HashiCorp Vault).
    - Restrict sensitive Actuator endpoints (`/actuator/env`, `/actuator/configprops`) to authenticated users or a
      separate management port.
- **Database**:
    - Set `spring.jpa.hibernate.ddl-auto: none` or `validate` to prevent schema changes in production.
- **Kafka**:
    - Configure replication factors (e.g., 3) and partitions for high availability.
    - Use SSL/TLS for secure Kafka communication.
- **Monitoring**:
    - Enable distributed tracing with Azure Application Insights or OpenTelemetry.
    - Collect metrics with Azure Monitor or Prometheus/Grafana.
- **Scaling**:
    - Deploy multiple instances with Eureka for load balancing.
    - Configure consumer group scaling with `order-service-group`.

### Monitoring and Actuator Endpoints

Spring Boot Actuator provides endpoints for monitoring the service’s health and configuration:

- **Endpoints**:
    - Health: `/actuator/health` (e.g., `http://localhost:8080/actuator/health` or
      `http://localhost:3001/orders/actuator/health` via API Gateway)
    - Info: `/actuator/info`
    - Environment: `/actuator/env`
    - Configuration Properties: `/actuator/configprops`

- **Access**:
    - These endpoints are publicly accessible for `GET` requests (no authentication required) as configured in
      `security.permit-urls`.
    - Test with:
      ```bash
      curl http://localhost:8080/actuator/health
      ```
      Expected output:
      ```json
      {"status":"UP"}
      ```

- **Production Notes**:
    - Restrict `/actuator/env` and `/actuator/configprops` to prevent sensitive data exposure.
    - Consider using a separate management port (e.g., `management.server.port: 8081`) for Actuator endpoints.

---

## Error Handling and Resiliency

The service implements robust error handling to ensure reliability and clear feedback.

### Business Exceptions

These represent failures in business logic, returning `4xx` HTTP status codes via the API Gateway or a global exception
handler.

- **`OrderValidationException`**:
    - **Purpose**: Thrown when order validation fails (e.g., insufficient inventory, duplicate items).
    - **Example**:
      ``` java
      throw new OrderValidationException(ExceptionError.ORDER_INSUFFICIENT_INVENTORY, item.productId());
      ```
- **`OrderCancellationException`**:
    - **Purpose**: Thrown when an order cannot be canceled due to its state.
    - **Example**:
      ``` java
      throw new OrderCancellationException("Order cannot be canceled in its current state");
      ```

### Infrastructure Exceptions

These represent system or external dependency failures, returning `5xx` HTTP status codes.

- **`EventPublishingException`**:
    - **Purpose**: Thrown when Kafka event serialization or publishing fails.
    - **Example**:
      ``` java
      throw new EventPublishingException("Failed to serialize or publish event", e);
      ```
- **`ConcurrencyException`**:
    - **Purpose**: Thrown for concurrency issues (e.g., `InterruptedException` in `StructuredTaskScope`).
    - **Example**:
      ``` java
      throw new ConcurrencyException("Validation process was interrupted", e);
      ```

### Kafka Error Handling

The service uses Spring Kafka’s `DefaultErrorHandler` for robust message processing:

- **Configuration**:
    - Defined in `KafkaErrorHandlerConfig` as a `CommonErrorHandler` bean.
    - Uses `ErrorHandlingDeserializer` to catch deserialization errors.
    - Configures 3 retries with a 1-second delay for transient errors (e.g., `NetworkException`).
    - Sends non-retriable errors (e.g., `SerializationException`) to `dead-letter-topic`.

- **Implementation**:
  ```java
  @Configuration
  public class KafkaErrorHandlerConfig {
      @Bean
      public CommonErrorHandler kafkaErrorHandler(KafkaTemplate<String, CloudEvent> kafkaTemplate) {
          DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
              kafkaTemplate, (record, ex) -> new TopicPartition("dead-letter-topic", -1)
          );
          DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 3));
          errorHandler.setRetryListeners((record, ex, attempt) -> {
              log.info("Retry attempt {} for record on topic {}: {}", attempt, record.topic(), ex.getMessage());
          });
          return errorHandler;
      }
  }
  ```

- **Listener Usage**:
  ```java
  @KafkaListener(topics = "order_created", containerFactory = "kafkaListenerContainerFactory")
  public void listen(ConsumerRecord<String, CloudEvent> record, Acknowledgment acknowledgment) {
      try {
          // Process event
          acknowledgment.acknowledge();
      } catch (Exception e) {
          log.error("Error processing record: {}", e.getMessage());
          throw e; // Handled by DefaultErrorHandler
      }
  }
  ```

- **Purpose**:
    - Prevents “poison pill” messages from blocking consumers.
    - Ensures retries for transient errors and DLT for unrecoverable errors.
    - Centralizes error handling, reducing listener code complexity.

### API Resiliency

- **Feign Clients**: Use `@JsonIgnoreProperties(ignoreUnknown = true)` on DTOs to handle unknown fields, ensuring
  compatibility with evolving APIs (e.g., `product-service`, `payment-service`).
- **Benefits**:
    - Prevents deserialization failures from breaking changes.
    - Promotes loose coupling and independent service evolution.

---

## Testing

- **Unit Tests**: Use JUnit 5 and Mockito for isolated testing of components (e.g., services, state machine).
- **Integration Tests**: Use Testcontainers to spin up MySQL and Kafka for realistic testing.
- **Run Tests**: `./gradlew test`

---

## Future Enhancements

### Performance Improvements

- **Caffeine Cache for LoadBalancer**:
    - **Why**: Improves service-to-service communication latency compared to the default cache.
    - **Implementation**:
      ```groovy
      implementation 'com.github.ben-manes.caffeine:caffeine'
      ```
      ```yaml
      spring:
        cloud:
          loadbalancer:
            cache:
              caffeine:
                spec: maximumSize=500,expireAfterAccess=5m
      ```

### Observability Enhancements

- **Structured Logging**:
    - **Why**: JSON logs enable better parsing by log aggregators (e.g., ELK, Grafana Loki).
    - **Implementation**:
      ```groovy
      implementation 'net.logstash.logback:logstash-logback-encoder:7.4'
      ```
      ```xml
      <!-- logback-spring.xml -->
      <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
          <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
      </appender>
      ```

- **Log Level Management**:
    - **Why**: Reduces clutter from verbose third-party logs.
    - **Implementation**:
      ```yaml
      logging:
        level:
          org.apache.kafka: WARN
          org.hibernate: WARN
          com.zaxxer.hikari: WARN
      ```

### Kafka Enhancements

- **Advanced DLT Processing**:
    - Add a dedicated consumer for `dead-letter-topic` to analyze and reprocess failed messages.
- **Schema Validation**:
    - Integrate a schema registry (e.g., Confluent Schema Registry) to validate `CloudEvent` payloads.
- **Consumer Group Scaling**:
    - Increase partitions and consumer instances for high-throughput topics.

### Security Enhancements

- **Role-Based Access**:
    - Extend `SecurityProperties` to include `endpoint-roles` for role-based access (e.g., `ROLE_ADMIN` for
      `/actuator/env`).
    - Example:
      ```yaml
      security:
        endpoint-roles:
          actuator:
            - path: /actuator/env/**
              method: GET
              role: ROLE_ADMIN
      ```

- **Management Port**:
    - Use a separate port for Actuator endpoints to isolate monitoring traffic:
      ```yaml
      management:
        server:
          port: 8081
      ```

- **Support for Non-GET Methods**:
    - Extend `SecurityProperties` to support specific HTTP methods for permitted URLs:
      ```yaml
      security:
        permit-urls:
          actuator:
            - path: /actuator/refresh
              methods: [POST]
      ```

---

## Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [Spring Kafka](https://docs.spring.io/spring-kafka/reference/html/)
- [CloudEvents](https://cloudevents.io/)
- [Spring State Machine](https://docs.spring.io/spring-statemachine/docs/current/reference)
- [Spring Cloud Config](https://cloud.spring.io/spring-cloud-config/)
- [Eureka](https://github.com/Netflix/eureka/wiki)
- [Springdoc OpenAPI](https://springdoc.org/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

---