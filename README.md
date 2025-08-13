# E-commerce Microservices

This is a personal project to explore microservices architecture, built with **Spring Boot 3.0**, **Gradle**, **Docker**, and **Kubernetes**. The project simulates an e-commerce platform where users can register, log in, browse products, manage shopping carts, create orders, process payments, and track shipments through a secure API Gateway.

[![Build Status](https://github.com/javiertuya/samples-test-spring/actions/workflows/build.yml/badge.svg)](https://github.com/javiertuya/samples-test-spring/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=my%3Asamples-test-spring&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=my%3Asamples-test-spring)
[![Javadoc](https://img.shields.io/badge/%20-javadoc-blue)](https://javiertuya.github.io/samples-test-spring/)

![Architecture Diagram](/config/images/Architecture_Software_final.png)

## About the Project

This project demonstrates a microservices-based e-commerce platform using modern technologies:
- **Spring Boot 3.0**: Framework for building microservices with Java 21.
- **Gradle**: Build tool for dependency management and multi-module project structure.
- **Spring Cloud Config**: Centralized configuration management (see [config-server/README.md](config-server/README.md)).
- **Eureka Server**: Service discovery for dynamic routing (see [service-registry/README.md](service-registry/README.md)).
- **Spring Cloud Gateway**: API Gateway for request routing, authentication, and rate limiting (see [api-gateway/README.md](api-gateway/README.md)).
- **Kafka**: Asynchronous event-driven communication between services.
- **Keycloak**: OAuth2-based authentication and authorization.
- **Docker & Kubernetes**: Containerization and orchestration for local and cloud deployments.
- **Monitoring & Tracing**:
    - Local: Zipkin (tracing), Prometheus (metrics), Grafana (visualization), AKHQ (Kafka monitoring).
    - Dev/Prod: Azure Application Insights and Azure Monitor for tracing, metrics, and logs.

For detailed multi-module setup, see [docs/multi-module.md](config/docs/multi-module.md).

## Services

| Service Type               | Service Name         | Description                                                               | Storage               | Documentation                                                    |
|----------------------------|----------------------|---------------------------------------------------------------------------|-----------------------|------------------------------------------------------------------|
| **Configuration Services** | Config Server        | Centralized configuration management for all services.                    | -                     | [config-server/README.md](config-server/README.md)               |
|                            | Eureka Server        | Service registry for dynamic discovery of microservices.                  | -                     | [service-registry/README.md](service-registry/README.md)         |
|                            | API Gateway          | Routes requests and handles authentication/authorization with Keycloak.   | Redis (rate limiting) | [api-gateway/README.md](api-gateway/README.md)                   |
| **Core Business Services** | User Service         | Manages user registration, login, and role-based authorization.           | PostgreSQL + Keycloak | [user-service/README.md](user-service/README.md)                 |
|                            | Product Service      | CRUD operations for products and inventory management.                    | MySQL                 | [product-service/README.md](product-service/README.md)           |
|                            | Order Service        | Processes orders with state machine, validation, and payment integration. | MySQL                 | [order-service/README.md](order-service/README.md)               |
|                            | Payment Service      | Handles payment processing with external gateways (e.g., Stripe).         | PostgreSQL            | [payment-service/README.md](payment-service/README.md)           |
|                            | Cart Service         | Manages user shopping carts for temporary item storage.                   | Redis                 | [cart-service/README.md](cart-service/README.md)                 |
|                            | Shipment Service     | Manages order fulfillment and shipping processes.                         | PostgreSQL            | [shipment-service/README.md](shipment-service/README.md)         |
|                            | Notification Service | Sends email, SMS, or push notifications based on order events.            | PostgreSQL (optional) | [notification-service/README.md](notification-service/README.md) |

For details on the order lifecycle, see [docs/order-state-machine.md](config/docs/order-state-machine.md).

## Quick Start

Get the project running locally in a few steps:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/puitiza/ecommerce.git
   cd ecommerce
   ```

2. **Start the Services**:
   ```bash
   docker-compose up --build -d
   ```

3. **Configure Keycloak for HTTP Access**:
   The Keycloak `master` realm requires configuration to allow HTTP in development:
   ```bash
   docker exec -it keycloak-server /bin/bash
   /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin --password admin
   /opt/keycloak/bin/kcadm.sh update realms/master -s sslRequired=NONE
   exit
   ```

4. **Access the Keycloak Admin Console**:
   Open `http://localhost:9090/admin` and log in with:
    - Username: `admin`
    - Password: `admin`

5. **Test the API Gateway**:
   Access Swagger UI at `http://localhost:8090/swagger-ui.html` to test endpoints.

## Setup and Installation

**Important**: The local setup uses HTTP for development. In production, enable HTTPS and configure Keycloak’s `sslRequired` to `external` or `all`. See [docs/production-setup.md](config/docs/production-setup.md).

### Prerequisites
- Java 21
- Gradle 8.0+
- Docker and Docker Compose
- Docker Desktop with Kubernetes enabled
- Postman (optional for testing)

### Local Setup
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/puitiza/ecommerce.git
   cd ecommerce
   ```

2. **Running the Application**:
    - **Build and start all services:**
      ```bash
      docker-compose up --build -d
      ```
    - **Stop and remove all services:**
      ```bash
      docker-compose down
      ```
    - **Stop and remove all services and volumes (clean up):**
      ```bash
      docker-compose down -v
      ```
    
    #### **Developer Workflow (Advanced)**
    For faster iterations during development, you can manage services individually:
    
   - **Rebuild and restart a single service (e.g., `order-service`):**
      ```bash
      docker-compose up --build --no-deps -d order-service
      ```
   - **Rebuild, remove and restart a service and its database (e.g., `order-service` and `mysql`):**
      ```bash
      docker-compose down -v order-service mysql && docker-compose up --build -d order-service mysql
      ```
   - **View logs for a specific service:**
      ```bash
      docker-compose logs <service-name>
      ```

3. **Access Services**:
    - API Gateway: `http://localhost:8090`
    - Swagger UI: `http://localhost:8090/swagger-ui.html`
    - Keycloak Admin Console: `http://localhost:9090/admin`
    - Eureka Dashboard: `http://localhost:8761`
    - Order Service: `http://localhost:8090/orders` (via API Gateway)
    - Payment Service: `http://localhost:8090/payments` (via API Gateway)
    - Product Service: `http://localhost:8090/products` (via API Gateway)

4. **Test Endpoints**:
   Import the Postman collection from `postman/ecommerce-collection.json` or use Swagger UI.

### Setting Up Keycloak for Local Development
Keycloak provides OAuth2-based authentication for the `user-service`, `order-service`, `payment-service`, `product-service`, and API Gateway. The `ecommerce` realm is imported from `config/imports/realm-export.json` with `sslRequired: "none"` for HTTP access. The `master` realm (used for the admin console) requires manual configuration to allow HTTP.

**Note**: This HTTP setup is for **development only**. In production, configure HTTPS and set `sslRequired` to `external` or `all`.

1. **Start the Keycloak Server**:
   Ensure the Keycloak server and PostgreSQL database are running:
   ```bash
   docker-compose up --build -d
   ```

2. **Configure the `master` Realm for HTTP Access**:
   ```bash
   docker exec -it keycloak-server /bin/bash
   /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin --password admin
   /opt/keycloak/bin/kcadm.sh update realms/master -s sslRequired=NONE
   exit
   ```

3. **Access the Keycloak Admin Console**:
   Open `http://localhost:9090/admin` and log in with:
    - Username: `admin`
    - Password: `admin`

4. **Test the `ecommerce` Realm**:
   Test with the `api-gateway-client`:
   ```bash
   http://localhost:9090/realms/ecommerce/protocol/openid-connect/auth?client_id=api-gateway-client&response_type=code&redirect_uri=http://localhost:8090
   ```

For Keycloak integration details, see individual service README's.

### Troubleshooting Keycloak
- **HTTPS Required Error**: Ensure the `kcadm.sh` commands were executed. Verify `sslRequired` is `None` in the `master` realm (Realm Settings > Login).
- **Client ID Null Error**: Check the `security-admin-console` client in the `master` realm has valid redirect URIs (`http://localhost:9090/admin/master/console/*`).
- **External IP Issues**: Access `http://localhost:9090` directly, avoid proxies/VPNs, and clear browser cache.

### Kubernetes Setup
See [docs/production-setup.md](config/docs/production-setup.md) for Kubernetes and Azure deployment instructions.

## Roles and Permissions
- **ADMIN**:
    - **User Service**: View all user details, update/delete users.
    - **Product Service**: Create, update, and delete products.
    - **Order Service**: View all orders, cancel any order.
    - **Shipment Service**: View all shipments, update shipment status.
- **USER**:
    - **User Service**: View/update own profile.
    - **Product Service**: Browse products.
    - **Cart Service**: Manage own cart.
    - **Order Service**: Create/view/cancel own orders.
    - **Shipment Service**: View own shipment status.
- **Internal (Service-to-Service)**:
    - **Payment Service**: Invoked by `order-service` via Kafka events.
    - **Shipment Service**: Invoked by `order-service` for shipment creation.
    - **Notification Service**: Triggered by Kafka events, no direct client access.
- **Authentication**:
    - All client-facing endpoints (except `POST /users/signup`, `POST /users/login`) require a JWT token from Keycloak.
    - Service-to-service communication uses API keys or Kubernetes network policies.

## Business Logic
See individual service READMES for detailed business logic:
- [order-service/README.md](order-service/README.md)
- [payment-service/README.md](payment-service/README.md)
- [product-service/README.md](product-service/README.md)
  
For the order lifecycle, see [docs/order-state-machine.md](config/docs/order-state-machine.md).

## Testing
- **Unit Tests**: JUnit 5 and Mockito for service logic.
- **Integration Tests**: Testcontainers for databases and Kafka.
- **Load Tests**: JMeter for API endpoint performance.
- **Configuration**: Run tests with `./gradlew test`.

## Monitoring and Tracing
- **Local**:
    - **Zipkin**: Distributed tracing (`http://localhost:9411`).
    - **Prometheus**: Metrics collection (`http://localhost:9090`).
    - **Grafana**: Metrics visualization (`http://localhost:3000`).
    - **AKHQ**: Kafka topic monitoring (`http://localhost:8081`).
- **Dev/Prod**:
    - **Azure Application Insights**: Tracing.
    - **Azure Monitor**: Metrics and logs.

## Troubleshooting
- **Keycloak Issues**: See [Troubleshooting Keycloak](#troubleshooting-keycloak).
- **Service Discovery**: If services fail to register with Eureka, check logs (`docker-compose logs service-registry`) and ensure the Eureka server is running at `http://localhost:8761`.
- **Database Connection Errors**: Verify PostgreSQL/MySQL/Redis containers are running (`docker ps`) and check credentials in `docker-compose.yml`.
- **API Gateway Errors**: Ensure the API Gateway is configured with Keycloak’s token endpoint and client credentials (see [api-gateway/README.md](api-gateway/README.md)).

## CI/CD
- **Pipeline**: GitHub Actions for building, testing, and deploying.
- **Steps**:
    - Run unit and integration tests with Gradle and Testcontainers.
    - Perform code quality analysis with SonarQube.
    - Build and push Docker images to Docker Hub.
    - Deploy to Kubernetes (local Docker Desktop or Azure AKS).
- **Configuration**: See `.github/workflows/build.yml`.

## Security
- **Authentication**: Keycloak with OAuth2 and JWT for client-facing endpoints.
- **Secrets**: Planned integration with Azure Key Vault for sensitive data (e.g., database passwords, Keycloak client secrets).
- **HTTPS**: Planned for production.
- **Rate Limiting**: Configured in API Gateway to prevent abuse.

## Production Deployment
For production setup with Azure, Kubernetes, and Application Insights, see [docs/production-setup.md](config/docs/production-setup.md).

## Future Enhancements
- **Review Service**: Manage product reviews and ratings (MongoDB).
- **Recommendation Service**: Suggest products based on user behavior (Neo4j or machine learning).
- **Analytics Service**: Analyze user and product data (Kafka Streams or Spark).
- **Internationalization**: Support multiple languages and currencies.
- **Fraud Detection**: Integrate Stripe Radar or custom rules for payment validation.
- **Multi-Module Enhancements**: Shared DTOs, exceptions, and utilities in the `share-library` module (see [docs/multi-module.md](config/docs/multi-module.md)).

## Contributing
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/new-feature`).
3. Follow coding standards (checkstyle, SonarQube).
4. Submit a pull request with a clear description of changes.

## Resources
- **Spring Cloud Config**: [Docker’s Health Check and Spring Boot Apps](https://medium.com/@aleksanderkolata/docker-spring-boot-and-containers-startup-order-39230e5352a4)
- **Keycloak**:
    - [Users and Client Secrets in Keycloak Realm Exports](https://candrews.integralblue.com/2021/09/users-and-client-secrets-in-keycloak-realm-exports/)
    - [Keycloak in Docker #5: Export a Realm with Users and Secrets](https://keepgrowing.in/tools/keycloak-in-docker-5-how-to-export-a-realm-with-users-and-secrets/)
- **Kubernetes Metrics Server**: [Enable Kubernetes Metrics Server on Docker Desktop](https://dev.to/docker/enable-kubernetes-metrics-server-on-docker-desktop-5434)
- **Example Projects**:
    - [Blog Application](https://github.com/cokutan/blogapplication/tree/develop)
    - [Spring Boot Microservices with Helm](https://github.com/numerica-ideas/community/tree/master/kubernetes/spring-microservice-deployment-gitlab-helm)