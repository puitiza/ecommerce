# E-commerce Microservices

This is a personal project to explore microservices architecture, split into sections and commits for clarity.

[![Build Status](https://github.com/javiertuya/samples-test-spring/actions/workflows/build.yml/badge.svg)](https://github.com/javiertuya/samples-test-spring/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=my%3Asamples-test-spring&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=my%3Asamples-test-spring)
[![Javadoc](https://img.shields.io/badge/%20-javadoc-blue)](https://javiertuya.github.io/samples-test-spring/)

![Architecture Diagram](https://github.com/puitiza/ecommerce/blob/main/images/Architecture%20Software%20final.png?raw=true)

## About the Project

This project is a Spring Boot 3.0-based e-commerce platform built with microservices, Docker, and Kubernetes. Users can register, log in, browse products, manage shopping carts, create orders, and process payments through a secure API Gateway. The architecture leverages:

- **Spring Cloud Config** for centralized configuration.
- **Eureka Server** for service discovery.
- **Spring Cloud Gateway** for routing and authentication.
- **Kafka** for asynchronous communication.
- **Zipkin, Prometheus, Grafana** (local) and **Azure Application Insights/Monitor** (dev/prod) for tracing and monitoring.
- **Keycloak** for authentication and authorization.

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
|                            | Notification Service | Sends email, SMS, or push notifications based on order events.            | PostgreSQL (optional) |

## Setup and Installation

### Prerequisites
- Java 17
- Gradle
- Docker and Docker Compose
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

### Kubernetes Setup
1. Install Minikube:
   ```bash
   brew install minikube
   minikube start
   ```
2. Install Metrics Server for resource metrics:
   ```bash
   kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
   ```
3. Convert Docker Compose to Kubernetes manifests:
   ```bash
   kompose convert -f docker.yml -o k8s/
   ```
4. Deploy to Kubernetes:
   ```bash
   kubectl apply -f k8s/
   ```
5. Access the API Gateway:
   ```bash
   minikube service api-gateway
   ```

## Business Logic

### User Service
- **Functionality**: User registration, login, and role-based authorization (ADMIN, USER) using Keycloak and JWT.
- **Storage**: PostgreSQL for user data, integrated with Keycloak.
- **Key Endpoints**:
    - `POST /users/signup`: Register a new user.
    - `POST /users/login`: Authenticate and obtain JWT.
    - `GET /users/{id}`: Retrieve user details (admin only).

### Product Service
- **Functionality**: Manages product catalog and inventory with CRUD operations.
- **Storage**: MySQL for product data.
- **Key Endpoints**:
    - `POST /products`: Create a product (admin only).
    - `GET /products`: List all products.
    - `GET /products/{id}`: Retrieve product details.
    - `POST /products/verify-availability`: Check inventory for order validation.

### Cart Service
- **Functionality**: Manages temporary shopping carts for users, allowing item additions and modifications.
- **Storage**: Redis for fast, ephemeral storage.
- **Key Endpoints**:
    - `POST /cart/{userId}/items`: Add item to cart.
    - `GET /cart/{userId}`: Retrieve cart.
    - `DELETE /cart/{userId}`: Clear cart.

### Order Service
- **Functionality**: Creates and manages orders with a state machine (`CREATED`, `VALIDATING`, `PAYMENT_PENDING`, `FULFILLED`, etc.).
- **Storage**: MySQL for order data.
- **Key Endpoints**:
    - `POST /orders`: Create an order from a cart.
    - `GET /orders/{orderId}`: Retrieve order details.
    - **State Machine**: Manages validation, payment, and fulfillment with retries and error handling (see `docs/order-state-machine.md`).
    - **Events**: Publishes Kafka events (`order_created`, `order_updated`) for asynchronous communication.

### Payment Service
- **Functionality**: Processes payments using external gateways (e.g., Stripe).
- **Storage**: PostgreSQL for transaction records.
- **Integration**: Consumes `order_created` events and updates order status via Kafka.

### Notification Service
- **Functionality**: Sends email, SMS, or push notifications based on order events.
- **Storage**: PostgreSQL for notification history (optional).
- **Integration**: Consumes Kafka events (`order_created`, `order_completed`) using AWS SES or Twilio.

### Business Flow
1. User logs in via `user-service` and receives a JWT.
2. User browses products (`product-service`) and adds items to cart (`cart-service`).
3. User creates an order (`order-service`), which validates product availability (`product-service`) and initiates payment (`payment-service`).
4. Upon successful payment, `order-service` updates the order status and publishes events to Kafka.
5. `notification-service` consumes events and sends notifications to the user.

### Benefits
- **Scalability**: Each microservice can scale independently.
- **Resilience**: Failures in one service don’t affect others.
- **Maintainability**: Small, focused codebases for each service.
- **Flexibility**: Asynchronous communication with Kafka enables loose coupling.

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
    - Run unit and integration tests with Maven and Testcontainers.
    - Perform code quality analysis with SonarQube.
    - Build and push Docker images to Docker Hub.
    - Deploy to Kubernetes (local Minikube or Azure AKS).
- **Configuration**: See `.github/workflows/build.yml`.

## Security
- **Authentication**: Keycloak with OAuth2 and JWT.
- **Secrets**: Planned integration with HashiCorp Vault for sensitive data.
- **HTTPS**: Planned for all services in production.
- **Rate Limiting**: Configured in API Gateway for abuse prevention.

## Future Enhancements
- **Review Service**: Manage product reviews and ratings (MongoDB).
- **Recommendation Service**: Suggest products based on user behavior (Neo4j or machine learning).
- **Analytics Service**: Analyze user and product data for insights (Kafka Streams or Spark).
- **Internationalization**: Support multiple languages and currencies.
- **Fraud Detection**: Integrate Stripe Radar or custom rules for payment validation.

## Order State Machine
The `order-service` uses a state machine to manage order lifecycles with error handling and retries. Key states include `CREATED`, `VALIDATING`, `PAYMENT_PENDING`, `FULFILLED`, and `CANCELLED`. For details, see `docs/order-state-machine.md`.

## Resources
- **Spring Cloud Config**: [Docker’s health check and Spring Boot apps](https://medium.com/@aleksanderkolata/docker-spring-boot-and-containers-startup-order-39230e5352a4)
- **Keycloak**:
    - [Users and Client Secrets in Keycloak Realm Exports](https://candrews.integralblue.com/2021/09/users-and-client-secrets-in-keycloak-realm-exports/)
    - [Keycloak in Docker #5](https://keepgrowing.in/tools/keycloak-in-docker-5-how-to-export-a-realm-with-users-and-secrets/)
- **Example Projects**:
    - [Blog Application](https://github.com/cokutan/blogapplication/tree/develop)
    - [Spring Boot Microservices with Helm](https://github.com/numerica-ideas/community/tree/master/kubernetes/spring-microservice-deployment-gitlab-helm)

## Contributing
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/new-feature`).
3. Follow coding standards (checkstyle, SonarQube).
4. Submit a pull request with a clear description of changes.