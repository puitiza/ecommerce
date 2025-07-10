# E-commerce Microservices

This is a personal project to explore microservices architecture, built with Spring Boot 3.0, Gradle, Docker, and Kubernetes. The project simulates an e-commerce platform where users can register, log in, browse products, manage shopping carts, create orders, process payments, and track shipments through a secure API Gateway.

[![Build Status](https://github.com/javiertuya/samples-test-spring/actions/workflows/build.yml/badge.svg)](https://github.com/javiertuya/samples-test-spring/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=my%3Asamples-test-spring&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=my%3Asamples-test-spring)
[![Javadoc](https://img.shields.io/badge/%20-javadoc-blue)](https://javiertuya.github.io/samples-test-spring/)

![Architecture Diagram](https://github.com/puitiza/ecommerce/blob/main/images/Architecture%20Software%20final.png?raw=true)

## About the Project

This project demonstrates a microservices-based e-commerce platform using modern technologies:
- **Spring Boot 3.0**: Framework for building microservices with Java 17 (planned upgrade to Java 21).
- **Gradle**: Build tool for dependency management and project compilation.
- **Spring Cloud Config**: Centralized configuration management.
- **Eureka Server**: Service discovery for dynamic routing.
- **Spring Cloud Gateway**: API Gateway for request routing and authentication.
- **Kafka**: Asynchronous event-driven communication between services.
- **Keycloak**: OAuth2-based authentication and authorization.
- **Docker & Kubernetes**: Containerization and orchestration for local and cloud deployments.
- **Monitoring & Tracing**:
    - Local: Zipkin (tracing), Prometheus (metrics), Grafana (visualization), AKHQ (Kafka monitoring).
    - Dev/Prod: Azure Application Insights and Azure Monitor for tracing, metrics, and logs.

## Services

| Service Type               | Service Name         | Description                                                               | Storage               |
|----------------------------|----------------------|---------------------------------------------------------------------------|-----------------------|
| **Configuration Services** | Config Server        | Centralized configuration management for all services.                    | -                     |
|                            | Eureka Server        | Service registry for dynamic discovery of microservices.                  | -                     |
|                            | API Gateway          | Routes requests and handles authentication/authorization with Keycloak.   | -                     |
| **Core Business Services** | User Service         | Manages user registration, login, and role-based authorization.           | PostgreSQL + Keycloak |
|                            | Product Service      | CRUD operations for products and inventory management.                    | MySQL                 |
|                            | Order Service        | Processes orders with state machine, validation, and payment integration. | MySQL                 |
|                            | Payment Service      | Handles payment processing with external gateways (e.g., Stripe).         | PostgreSQL            |
|                            | Cart Service         | Manages user shopping carts for temporary item storage.                   | Redis                 |
|                            | Shipment Service     | Manages order fulfillment and shipping processes.                         | PostgreSQL            |
|                            | Notification Service | Sends email, SMS, or push notifications based on order events.            | PostgreSQL (optional) |

## Setup and Installation

### Prerequisites
- Java 17 (planned upgrade to Java 21)
- Gradle 8.0+
- Docker and Docker Compose
- Docker Desktop with Kubernetes enabled
- Postman (optional for testing)

### Local Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/puitiza/ecommerce.git
   cd ecommerce
   ```
2. Build and run services:
   ```bash
   docker-compose up --build -d
   ```
3. Access the API Gateway at `http://localhost:8090`.
4. Use Swagger UI (`http://localhost:8090/swagger-ui.html`) or Postman to test endpoints.
5. Import the Postman collection from `postman/ecommerce-collection.json`.

#### Useful Docker Compose Commands
- Build and start services: `docker-compose up --build -d`
- Stop and remove services: `docker-compose down`
- Remove volumes (clean up): `docker-compose down -v`
- View logs: `docker-compose logs <service-name>`

### Kubernetes Setup
1. Enable Kubernetes in Docker Desktop:
    - Open Docker Desktop > Settings > Kubernetes > Enable Kubernetes > Apply & Restart.
2. Install Metrics Server to enable resource metrics:
   ```bash
   kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.8.0/components.yaml
   ```
3. Apply Metrics Server patch for Docker Desktop:
   ```bash
   kubectl patch deployment metrics-server -n kube-system --type='json' -p='[{"op": "add", "path": "/spec/template/spec/containers/0/args/-", "value": "--kubelet-insecure-tls"}]'
   ```
4. Convert Docker Compose to Kubernetes manifests:
   ```bash
   kompose convert -f docker.yml -o k8s/
   ```
5. Deploy to Kubernetes:
   ```bash
   kubectl apply -f k8s/
   ```
6. Access the API Gateway:
   ```bash
   kubectl port-forward svc/api-gateway 8090:8090
   ```
7. Verify metrics:
   ```bash
   kubectl top pods
   ```

## Roles and Permissions
- **ADMIN**:
    - **User Service**: View all user details (`GET /users`), update/delete users (`PUT /users/{id}`, `DELETE /users/{id}`).
    - **Product Service**: Create, update, and delete products (`POST /products`, `PUT /products/{id}`, `DELETE /products/{id}`).
    - **Order Service**: View all orders (`GET /orders`), cancel any order (`POST /orders/{orderId}/cancel`).
    - **Shipment Service**: View all shipments (`GET /shipments`), update shipment status (`PUT /shipments/{id}`).
- **USER**:
    - **User Service**: View own profile (`GET /users/me`), update own profile (`PUT /users/me`).
    - **Product Service**: Browse products (`GET /products`, `GET /products/{id}`).
    - **Cart Service**: Manage own cart (`POST /cart/{userId}/items`, `GET /cart/{userId}`, `DELETE /cart/{userId}`).
    - **Order Service**: Create and view own orders (`POST /orders`, `GET /orders/{orderId}`), cancel own orders (`POST /orders/{orderId}/cancel`).
    - **Shipment Service**: View own shipment status (`GET /shipments/{orderId}`).
- **Internal (Service-to-Service)**:
    - **Payment Service**: Invoked by `order-service` via internal API calls (e.g., `POST /payments`) using service credentials or network trust.
    - **Shipment Service**: Invoked by `order-service` for shipment creation (`POST /shipments`) using service credentials.
    - **Notification Service**: Triggered by Kafka events, no direct client access.
- **Authentication**:
    - All client-facing endpoints (except `POST /users/signup`, `POST /users/login`) require a JWT token from Keycloak.
    - Service-to-service communication uses API keys or Kubernetes network policies for security.

## Business Logic

### User Service
- **Functionality**: User registration, login, and role-based authorization (ADMIN, USER) using Keycloak and JWT.
- **Storage**: PostgreSQL for user data, integrated with Keycloak.
- **Key Endpoints**:
    - `POST /users/signup`: Register a new user (public).
    - `POST /users/login`: Authenticate and obtain JWT (public).
    - `GET /users/{id}`: Retrieve user details (ADMIN only).
    - `GET /users/me`: Retrieve own profile (USER).
    - `PUT /users/me`: Update own profile (USER).

### Product Service
- **Functionality**: Manages product catalog and inventory with CRUD operations.
- **Storage**: MySQL for product data.
- **Key Endpoints**:
    - `POST /products`: Create a product (ADMIN only).
    - `GET /products`: List all products (USER, ADMIN).
    - `GET /products/{id}`: Retrieve product details (USER, ADMIN).
    - `POST /products/verify-availability`: Check inventory for order validation (ORDER-SERVICE, internal).

### Cart Service
- **Functionality**: Manages temporary shopping carts for users, allowing item additions and modifications.
- **Storage**: Redis for fast, ephemeral storage.
- **Key Endpoints**:
    - `POST /cart/{userId}/items`: Add item to cart (USER).
    - `GET /cart/{userId}`: Retrieve cart (USER).
    - `DELETE /cart/{userId}`: Clear cart (USER).

### Order Service
- **Functionality**: Creates and manages orders with a state machine (`CREATED`, `VALIDATING`, `PAYMENT_PENDING`, `SHIPPING`, `FULFILLED`, etc.).
- **Storage**: MySQL for order data.
- **Key Endpoints**:
    - `POST /orders`: Create an order from a cart (USER).
    - `GET /orders/{orderId}`: Retrieve order details (USER for own orders, ADMIN for all).
    - `POST /orders/{orderId}/cancel`: Cancel an order (USER, ADMIN).
    - **State Machine**: Manages validation, payment, and shipping with retries and error handling (see `docs/order-state-machine.md`).
    - **Events**: Publishes Kafka events (`order_created`, `order_updated`, `order_cancelled`) for asynchronous communication.

### Payment Service
- **Functionality**: Processes payments using external gateways (e.g., Stripe).
- **Storage**: PostgreSQL for transaction records.
- **Key Endpoints**:
    - `POST /payments`: Process payment for an order (ORDER-SERVICE, internal).
- **Integration**: Consumes `order_created` events and publishes `payment_initiated`, `payment_failed` events via Kafka.

### Shipment Service
- **Functionality**: Manages order fulfillment and shipping, integrating with external logistics APIs (e.g., FedEx, DHL).
- **Storage**: PostgreSQL for shipment records.
- **Key Endpoints**:
    - `POST /shipments`: Create a shipment for an order (ORDER-SERVICE, internal).
    - `GET /shipments/{orderId}`: Retrieve shipment status (USER for own orders, ADMIN for all).
    - `PUT /shipments/{id}`: Update shipment status (ADMIN only).
- **Integration**: Consumes `payment_initiated` events and publishes `shipment_created`, `shipment_delivered`, `shipment_failed` events via Kafka.

### Notification Service
- **Functionality**: Sends email, SMS, or push notifications based on order and shipment events.
- **Storage**: PostgreSQL for notification history (optional).
- **Integration**: Consumes Kafka events (`order_created`, `order_cancelled`, `shipment_delivered`, `shipment_failed`) using AWS SES or Twilio. No client-facing endpoints.

### Business Flow
1. User logs in via `user-service` and receives a JWT (USER or ADMIN role).
2. User browses products (`product-service`) and adds items to cart (`cart-service`).
3. User creates an order (`order-service`), which validates product availability (`product-service`).
4. Order transitions to `PAYMENT_PENDING`, and `payment-service` processes the payment.
5. If payment fails, order transitions to `PAYMENT_FAILED` with retries or cancellation.
6. On successful payment, order transitions to `SHIPPING`, and `shipment-service` handles the shipping process.
7. If shipping fails, order transitions to `SHIPPING_FAILED` with retries or cancellation.
8. On successful shipping, order transitions to `FULFILLED`, and `notification-service` sends a confirmation.
9. At any point, the user or admin can cancel the order, transitioning to `CANCELLED`.

### Benefits
- **Scalability**: Each microservice can scale independently.
- **Resilience**: Failures in one service don’t affect others.
- **Maintainability**: Small, focused codebases for each service.
- **Flexibility**: Asynchronous communication with Kafka enables loose coupling.

## Testing
- **Unit Tests**: JUnit 5 and Mockito for testing service logic.
- **Integration Tests**: Testcontainers for simulating databases and Kafka.
- **Load Tests**: JMeter for performance testing of API endpoints.
- **Configuration**: Run tests with `./gradlew test`.

## Monitoring and Tracing
- **Local**:
    - **Zipkin**: Distributed tracing (`http://localhost:9411`).
    - **Prometheus**: Metrics collection (`http://localhost:9090`).
    - **Grafana**: Metrics visualization (`http://localhost:3000`).
    - **AKHQ**: Kafka topic monitoring (`http://localhost:8081`).
- **Dev/Prod**:
    - **Azure Application Insights**: Replaces Zipkin for tracing.
    - **Azure Monitor**: Replaces Prometheus/Grafana for metrics and logs.

## CI/CD
- **Pipeline**: GitHub Actions for building, testing, and deploying.
- **Steps**:
    - Run unit and integration tests with Gradle and Testcontainers.
    - Perform code quality analysis with SonarQube.
    - Build and push Docker images to Docker Hub.
    - Deploy to Kubernetes (local Docker Desktop or Azure AKS).
- **Configuration**: See `.github/workflows/build.yml`.

## Security
- **Authentication**: Keycloak with OAuth2 and JWT for all client-facing endpoints (except `POST /users/signup`, `POST /users/login`). Service-to-service communication uses API keys or Kubernetes network policies.
- **Secrets**: Planned integration with HashiCorp Vault for sensitive data.
- **HTTPS**: Planned for all services in production.
- **Rate Limiting**: Configured in API Gateway to prevent abuse.

## Future Enhancements
- **Review Service**: Manage product reviews and ratings (MongoDB).
- **Recommendation Service**: Suggest products based on user behavior (Neo4j or machine learning).
- **Analytics Service**: Analyze user and product data for insights (Kafka Streams or Spark).
- **Internationalization**: Support multiple languages and currencies.
- **Fraud Detection**: Integrate Stripe Radar or custom rules for payment validation.

## Order State Machine
The `order-service` uses a state machine to manage order lifecycles with error handling and retries. Key states include `CREATED`, `VALIDATING`, `PAYMENT_PENDING`, `SHIPPING`, `FULFILLED`, and `CANCELLED`. For details, see `docs/order-state-machine.md`.

## Resources
- **Spring Cloud Config**: [Docker’s health check and Spring Boot apps](https://medium.com/@aleksanderkolata/docker-spring-boot-and-containers-startup-order-39230e5352a4)
- **Keycloak**:
    - [Users and Client Secrets in Keycloak Realm Exports](https://candrews.integralblue.com/2021/09/users-and-client-secrets-in-keycloak-realm-exports/)
    - [Keycloak in Docker #5](https://keepgrowing.in/tools/keycloak-in-docker-5-how-to-export-a-realm-with-users-and-secrets/)
- **Kubernetes Metrics Server**: [Enable Kubernetes Metrics Server on Docker Desktop](https://dev.to/docker/enable-kubernetes-metrics-server-on-docker-desktop-5434)
- **Example Projects**:
    - [Blog Application](https://github.com/cokutan/blogapplication/tree/develop)
    - [Spring Boot Microservices with Helm](https://github.com/numerica-ideas/community/tree/master/kubernetes/spring-microservice-deployment-gitlab-helm)

## Contributing
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/new-feature`).
3. Follow coding standards (checkstyle, SonarQube).
4. Submit a pull request with a clear description of changes.