# Payment Service

The Payment Service is a dedicated microservice responsible for handling payment processing within the e-commerce
platform. It integrates with external payment gateways and securely manages payment-related transactions.

## Key Features

- **Payment Processing**: Processes payment requests from the Order Service.
- **Transaction Management**: Records and manages payment transactions.
- **External Gateway Integration**: Designed to integrate with external payment gateways (e.g., Stripe, PayPal).
- **RESTful API**: Exposes a clear API for payment operations.
- **Security**: Secured with JWT authentication via Keycloak.

## Technologies

- **Spring Boot 3.0**: Framework for building robust microservices.
- **PostgreSQL**: Relational database for persistent storage of payment data.
- **Spring Data JPA / Hibernate**: For data persistence and ORM.
- **Spring Security**: For authentication and authorization using OAuth2 Resource Server.
- **Spring Cloud Config Client**: To fetch centralized configurations.
- **Eureka Client**: For service registration and discovery.
- **Springdoc OpenAPI**: For API documentation and Swagger UI.

## Configuration

The Payment Service retrieves its configuration from the Spring Cloud Config Server and uses Spring Boot's
autoconfiguration for JPA and OAuth2.

### Security Configuration

The service is secured with Keycloak OAuth2 and JWT. Spring Boot automatically configures a `JwtDecoder` using the
`issuer-uri` to validate tokens. Public endpoints (e.g., Swagger) are accessible without authentication

### Dependencies

- `spring-boot-starter-web`: For REST APIs.
- `spring-boot-starter-data-jpa`: Auto-configures PostgreSQL connectivity.
- `spring-boot-starter-security`: Enables OAuth2 resource server.
- `spring-boot-starter-oauth2-resource-server`: Configures JWT validation.
- `spring-kafka`: For consuming order events from Kafka.
- `spring-cloud-starter-config`: Connects to Config Server.
- `spring-cloud-starter-netflix-eureka-client`: Registers with Eureka.

## Integration with Other Services

- **Config Server**: Retrieves configuration (e.g., database credentials, Keycloak URL) from `http://localhost:8885`.
- **Eureka Server**: Registers as `PAYMENT-SERVICE` for service discovery.
- **API Gateway**: Routes requests from `/payments/**` to this service.
- **Keycloak**: Validates JWT tokens for authenticated requests.
- **Kafka**: Consumes order events (e.g., `ORDER_CREATED`) to initiate payment processing.
- **PostgreSQL**: Stores payment data with automatic schema updates (`ddl-auto: update`).

## Local Setup

To run the Payment Service locally:

1. Ensure [Config Server](https://www.google.com/search?q=config-server/README.md)
   and [Eureka Server](https://www.google.com/search?q=service-registry/README.md) are running.
2. Start the PostgreSQL container (if not using `docker-compose up`).
3. Navigate to the `payment-service` directory.
4. Run the application: `./gradlew bootRun` or use your IDE.
5. Alternatively, use `docker-compose up -d payment-service` from the root directory to start it as part of the overall
   microservices stack.

## Testing

- **Unit Tests**: Implemented using JUnit 5 and Mockito for isolated component testing.
- **Integration Tests**: Utilizes Testcontainers for database integration testing.
- Run tests with `./gradlew test`.

## Production Considerations

- Ensure database credentials and external payment gateway API keys are securely managed using environment variables or
  a secrets management solution (e.g., Azure Key Vault).
- Configure `spring.jpa.hibernate.ddl-auto` to `none` or `validate` in production for schema stability.
- Utilize distributed tracing (Azure Application Insights) and metrics (Azure Monitor) for production monitoring.
- Implement robust error handling and retry mechanisms for external payment gateway calls.

## Multi-Module Integration

The Payment Service integrates with the `share-library` module for shared DTOs, exceptions, and utility classes, ensuring
consistency across the microservices' ecosystem.

## Resources

- [Spring Cloud Config Documentation](https://cloud.spring.io/spring-cloud-config/)
- [Spring Boot Data JPA Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/data.html)
- [Spring Security OAuth2 Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

