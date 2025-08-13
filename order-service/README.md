# Order Service

The Order Service is a critical core business microservice responsible for managing the entire lifecycle of customer
orders within the e-commerce platform. It handles order creation, validation, state transitions, and integration with
other services like Payment and Shipment via a message broker (Kafka) and a REST client (Feign).

## Key Features

- **Order Lifecycle Management**: Manages orders through various states (e.g., PENDING, PROCESSING, SHIPPED, DELIVERED,
  CANCELLED) using a state machine.
- **Order Validation**: Ensures order integrity before processing.
- **Integration with Payment Service**: Initiates payment processing for new orders.
- **Integration with Product Service**: Interacts for product availability and inventory updates.
- **Integration with Shipment Service**: Triggers shipment creation upon successful payment.
- **Event-Driven Communication**: Utilizes Kafka for asynchronous communication and event publishing (e.g., order
  created, order paid).
- **RESTful API**: Provides endpoints for order creation, retrieval, and updates.
- **Security**: Secured with JWT authentication via Keycloak.

## Technologies

- **Spring Boot 3.0**: Framework for building robust microservices.
- **MySQL**: Relational database for persistent storage of order data.
- **Spring Data JPA / Hibernate**: For data persistence and ORM.
- **Apache Kafka**: For asynchronous, event-driven communication.
- **CloudEvents**: Used as the messaging format over Kafka for structured event data.
- **Spring Security**: For authentication and authorization using OAuth2 Resource Server.
- **Spring Cloud Config Client**: To fetch centralized configurations.
- **Eureka Client**: For service registration and discovery.
- **Springdoc OpenAPI**: For API documentation and Swagger UI.

## Configuration

The Order Service retrieves its configuration from the Spring Cloud Config Server and uses Spring Boot's
autoconfiguration for JPA, OAuth2, and Kafka.

### Security Configuration

The service is secured with Keycloak OAuth2 and JWT. Spring Boot automatically configures a `JwtDecoder` using the
`issuer-uri` to validate tokens. Public endpoints (e.g., Swagger) are accessible without authentication.

### Dependencies

- `spring-boot-starter-web`: For REST APIs.
- `spring-boot-starter-data-jpa`: Auto-configures MySQL connectivity.
- `spring-boot-starter-security`: Enables OAuth2 resource server.
- `spring-boot-starter-oauth2-resource-server`: Configures JWT validation.
- `spring-kafka`: For event publishing to Kafka.
- `spring-cloud-starter-config`: Connects to Config Server.
- `spring-cloud-starter-netflix-eureka-client`: Registers with Eureka.

## Integration with Other Services

- **Config Server**: Retrieves configuration (e.g., database credentials, Keycloak URL) from `http://localhost:8885`.
- **Eureka Server**: Registers as `ORDER-SERVICE` for service discovery.
- **API Gateway**: Routes requests from `/orders/**` to this service.
- **Keycloak**: Validates JWT tokens for authenticated requests.
- **Kafka**: Publishes order events (e.g., `ORDER_CREATED`, `ORDER_CANCELLED`) to topics consumed by `payment-service`,
  `shipment-service`, and `notification-service`.
- **MySQL**: Stores order data with automatic schema updates (`ddl-auto: update`).

## Local Setup

To run the Order Service locally:

1. Ensure [Config Server](https://www.google.com/search?q=config-server/README.md), [Eureka Server](https://www.google.com/search?q=service-registry/README.md),
MySQL, and Kafka are running.
2. Navigate to the `order-service` directory.
3. Run the application: `./gradlew bootRun` or use your IDE.
4. Alternatively, use `docker-compose up -d order-service` from the root directory to start it as part of the overall
   microservices stack.

## Testing

- **Unit Tests**: Implemented using JUnit 5 and Mockito for isolated component testing.
- **Integration Tests**: Utilizes Testcontainers for database and Kafka integration testing.
- Run tests with `./gradlew test`.

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

```yaml
logging:
  level:
    org.apache.kafka: WARN
    org.hibernate: WARN
    com.zaxxer.hikari: WARN
```

## Multi-Module Integration

The Order Service integrates with the `share-library` module for shared DTOs, exceptions, and utility classes, ensuring
consistency across the microservices' ecosystem.

## Resources

- [Spring Cloud Config Documentation](https://cloud.spring.io/spring-cloud-config/)
- [Spring Boot Data JPA Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html)
- [Spring Security OAuth2 Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [CloudEvents Specification](https://cloudevents.io/)