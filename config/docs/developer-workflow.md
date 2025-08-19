# Developer Workflow

This document provides advanced workflows for developers working on the e-commerce microservices platform. It covers
iterative development, debugging, and testing tasks using **Docker**, **Docker Compose**, **Gradle**, and other tools.
This guide assumes you have completed the basic setup in the [main README](/README.md) and are familiar with the
project's architecture.

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Managing Services with Docker Compose](#managing-services-with-docker-compose)
    - [Starting Specific Services](#starting-specific-services)
    - [Rebuilding and Restarting Services](#rebuilding-and-restarting-services)
    - [Stopping and Cleaning Up](#stopping-and-cleaning-up)
4. [Debugging Services](#debugging-services)
    - [Accessing Logs](#accessing-logs)
    - [Debugging with Actuator Endpoints](#debugging-with-actuator-endpoints)
    - [Tracing with Zipkin](#tracing-with-zipkin)
    - [Monitoring Kafka Topics with AKHQ](#monitoring-kafka-topics-with-akhq)
5. [Running Tests](#running-tests)
    - [Unit Tests](#unit-tests)
    - [Integration Tests with Testcontainers](#integration-tests-with-testcontainers)
    - [Load Tests with JMeter](#load-tests-with-jmeter)
6. [Iterative Development](#iterative-development)
    - [Hot Reloading with Gradle](#hot-reloading-with-gradle)
    - [Updating Configurations](#updating-configurations)
    - [Working with Keycloak](#working-with-keycloak)
7. [Troubleshooting Common Issues](#troubleshooting-common-issues)
    - [Service Registration Failures](#service-registration-failures)
    - [Database Connectivity Issues](#database-connectivity-issues)
    - [Kafka Consumer Errors](#kafka-consumer-errors)
8. [Best Practices](#best-practices)
9. [Resources](#resources)

---

## Overview

This guide is designed for developers contributing to the e-commerce platform. It provides detailed instructions for
managing services, debugging issues, running tests _

System: _, and optimizing workflows during development. The goal is to enable fast iteration cycles, effective
debugging, and reliable testing while working on individual microservices like `order-service`, `product-service`, or
`api-gateway`. By following these workflows, developers can efficiently modify code, test changes, and diagnose issues
in a local environment.

---

## Prerequisites

Before starting, ensure you have the following tools installed and configured as described in
the [main README](/README.md):

- **Java 21**: Required for Spring Boot 3.0.
- **Gradle 8.0+**: For building and testing.
- **Docker and Docker Compose**: For running services and dependencies.
- **Docker Desktop**: With Kubernetes enabled for local orchestration (optional).
- **Postman**: For manual API testing (optional).
- **IDE**: IntelliJ IDEA, VS Code, or Eclipse with Spring Tools for debugging and hot reloading.

Clone the repository if you haven’t already:

```bash
git clone https://github.com/puitiza/ecommerce.git
cd ecommerce
```

---

## Managing Services with Docker Compose

Docker Compose is the primary tool for running and managing the microservices stack locally. This section covers
advanced commands for starting, rebuilding, and stopping services.

### Starting Specific Services

To start a single service (e.g., `order-service`) and its dependencies:

```bash
docker-compose up -d order-service
```

> **Note**: Dependencies like MySQL, Kafka, or Keycloak are automatically started if defined in `docker-compose.yml`.

To start multiple specific services:

```bash
docker-compose up -d order-service product-service api-gateway
```

### Rebuilding and Restarting Services

To rebuild and restart a single service after code changes:

```bash
docker-compose up --build --no-deps -d order-service
```

- `--build`: Rebuilds the Docker image for the service.
- `--no-deps`: Avoids restarting dependent services (e.g., MySQL, Kafka).

To rebuild a service and its database (e.g., `order-service` and `mysql`):

```bash
docker-compose down -v order-service mysql && docker-compose up --build -d order-service mysql
```

- `-v`: Removes associated volumes (e.g., database data) for a clean start.

### Stopping and Cleaning Up

To stop a specific service:

```bash
docker-compose stop order-service
```

To stop and remove a service and its volumes:

```bash
docker-compose rm -f -v order-service
```

To stop and clean up the entire stack:

```bash
docker-compose down -v
```

---

## Debugging Services

Debugging microservices requires inspecting logs, monitoring health, and tracing requests. This section covers tools and
techniques for diagnosing issues.

### Accessing Logs

View logs for a specific service:

```bash
docker-compose logs order-service
```

Follow logs in real-time:

```bash
docker-compose logs -f order-service
```

Filter logs for errors:

```bash
docker-compose logs order-service | grep ERROR
```

> **Tip**: Use structured logging (JSON format) for easier parsing with tools like Grafana Loki.
> See [Future Enhancements](/README.md#future-enhancements) for implementation details.

### Debugging with Actuator Endpoints

Each service exposes Spring Boot Actuator endpoints for health and diagnostics:

- Health: `http://localhost:8080/actuator/health` (or `http://localhost:8090/<service-name>/actuator/health` via API
  Gateway)
- Info: `http://localhost:8080/actuator/info`
- Environment: `http://localhost:8080/actuator/env`

Test the health of `order-service`:

```bash
curl http://localhost:8080/orders/actuator/health
```

Expected output:

```json
{
  "status": "UP"
}
```

> **Note**: Restrict sensitive endpoints (`/actuator/env`, `/actuator/configprops`) in production.
> See [order-service/README.md](/order-service/README.md#monitoring-and-actuator-endpoints).

### Tracing with Zipkin

Use Zipkin for distributed tracing to debug service interactions:

- Access: `http://localhost:9411`
- Search for traces by service name (e.g., `order-service`) or request ID.
- Example: Trace a failed order creation to identify latency or errors in `payment-service` or `product-service`.

### Monitoring Kafka Topics with AKHQ

Inspect Kafka topics and messages using AKHQ:

- Access: `http://localhost:8081`
- Check topics like `order_created`, `payment_succeeded`, or `dead-letter-topic` for message flow and errors.
- Verify consumer group offsets for `order-service-group` to ensure messages are processed.

---

## Running Tests

The project includes unit, integration, and load tests to ensure reliability and performance.

### Unit Tests

Unit tests use JUnit 5 and Mockito to test service logic in isolation:

```bash
./gradlew :order-service:test
```

- Location: `src/test/java` in each service module.
- Example: Tests for `OrderService` validate state machine transitions.

### Integration Tests with Testcontainers

Integration tests use Testcontainers to spin up MySQL and Kafka:

```bash
./gradlew :order-service:integrationTest
```

- Location: `src/integrationTest/java`.
- Example: Tests for `OrderEventProcessor` verify Kafka event processing and database updates.

> **Tip**: Ensure Docker is running, as Testcontainers requires it to create containers.

### Load Tests with JMeter

Run load tests to evaluate API performance:

1. Start the stack: `docker-compose up -d`.
2. Import `postman/ecommerce-collection.json` into Postman or use JMeter scripts in `jmeter/`.
3. Run:
   ```bash
   jmeter -n -t jmeter/load-test.jmx -l results.jtl
   ```

- Analyze results for throughput, latency, and error rates.

---

## Iterative Development

This section covers workflows for rapid development cycles, including hot reloading, configuration updates, and Keycloak
integration.

### Hot Reloading with Gradle

To enable hot reloading for a service (e.g., `order-service`):

1. Run the service with Gradle’s continuous build:
   ```bash
   cd order-service
   ./gradlew bootRun --continuous
   ```
2. Make code changes; the application restarts automatically.

> **Note**: Hot reloading is ideal for rapid iteration but may not work with Docker Compose. Use
`docker-compose up --build` for containerized changes.

### Updating Configurations

Configurations are managed by Spring Cloud Config Server (`http://localhost:8885`). To update a service’s configuration:

1. Modify the configuration in `config-server/config/<service-name>.yml`.
2. Refresh the service:
   ```bash
   curl -X POST http://localhost:8080/<service-name>/actuator/refresh
   ```
   Example for `order-service`:
   ```bash
   curl -X POST http://localhost:8080/orders/actuator/refresh
   ```

> **Warning**: Secure the `/actuator/refresh` endpoint in production with authentication.

### Working with Keycloak

To debug Keycloak-related issues:

1. Access the admin console: `http://localhost:9090/admin` (Username: `admin`, Password: `admin`).
2. Verify the `ecommerce` realm settings and `api-gateway-client` configuration.
3. Test token issuance:
   ```bash
   curl -X POST http://localhost:9090/realms/ecommerce/protocol/openid-connect/token \
   -d "client_id=api-gateway-client&grant_type=password&username=user&password=password"
   ```

For detailed Keycloak setup, see [Keycloak Configuration](/README.md#keycloak-configuration).

---

## Troubleshooting Common Issues

### Service Registration Failures

If a service fails to register with Eureka:

- Check Eureka logs:
  ```bash
  docker-compose logs service-registry
  ```
- Ensure the Eureka server is running: `http://localhost:8761`.
- Verify the service’s `application.yml` has the correct Eureka URL:
  ```yaml
  eureka:
    client:
      service-url:
        defaultZone: http://localhost:8761/eureka/
  ```

### Database Connectivity Issues

If a service cannot connect to its database:

- Verify the database container is running:
  ```bash
  docker ps | grep mysql
  ```
- Check credentials in `docker-compose.yml` or `config-server/config/<service-name>.yml`.
- Reset the database:
  ```bash
  docker-compose down -v mysql && docker-compose up -d mysql
  ```

### Kafka Consumer Errors

If Kafka consumers fail to process messages:

- Check AKHQ (`http://localhost:8081`) for `dead-letter-topic` messages.
- Verify consumer group offsets for `order-service-group`.
- Restart the consumer:
  ```bash
  docker-compose restart order-service
  ```

For advanced Kafka error handling, see [order-service/README.md](/order-service/README.md#kafka-error-handling).

---

## Best Practices

- **Commit Small Changes**: Make frequent, small commits to isolate changes and simplify debugging.
- **Use Descriptive Logs**: Include context in log messages (e.g., order ID, user ID) for easier tracing.
- **Test Incrementally**: Run unit tests after each change; reserve integration tests for major updates.
- **Monitor Resources**: Watch Docker Desktop’s resource usage to avoid memory or CPU bottlenecks.
- **Secure Secrets**: Avoid hardcoding sensitive data; use environment variables or a secrets manager.

---

## Resources

- **Docker Compose**: [Official Documentation](https://docs.docker.com/compose/)
- **Spring Boot Actuator**: [Monitoring and Management](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- **Zipkin**: [Distributed Tracing](https://zipkin.io/)
- **AKHQ**: [Kafka Monitoring](https://akhq.io/)
- **Testcontainers**: [Integration Testing](https://www.testcontainers.org/)
- **JMeter**: [Load Testing](https://jmeter.apache.org/)

---