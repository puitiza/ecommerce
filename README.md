# ecommerce

This is a personal project in order to understand better microservices,
and it split by section and commits.

[![Build Status](https://travis-ci.org/joemccann/dillinger.svg?branch=master)](https://travis-ci.org/joemccann/dillinger)
[![Build Status](https://github.com/javiertuya/samples-test-spring/actions/workflows/build.yml/badge.svg)](https://github.com/javiertuya/samples-test-spring/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=my%3Asamples-test-spring&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=my%3Asamples-test-spring)
[![Javadoc](https://img.shields.io/badge/%20-javadoc-blue)](https://javiertuya.github.io/samples-test-spring/)

<img src="https://github.com/puitiza/ecommerce/blob/main/images/Architecture%20Software%20final.png?raw=true" alt="architecture diagram">     


# About the project

<ul style="list-style-type:disc">
These stages include checking out code, performing quality code analysis, building and pushing Docker images,
and deploying the services on a Kubernetes cluster.

The architecture uses several services and tools for different purposes:

  <li>This project is based Spring Boot 3.0 Microservices with the usage of Docker and Kubernetes</li>
  <li>User can register and login through USER-SERVICE by user role (ADMIN or USER) through api gateway</li>
  <li>User can send any request to relevant service through api gateway with its bearer token</li>
</ul>

7 services whose name are shown below have been devised within the scope of this project.

| Service Type               | Service Name    | Description                                                                           |
|----------------------------|-----------------|---------------------------------------------------------------------------------------|
| **Configuration Services** | Config Server   | Provides centralized configuration management for all services.                       |
|                            | Eureka Server   | Acts as a service registry, enabling services to discover each other dynamically.     |
|                            | API Gateway     | Routes requests to appropriate services and handles authentication and authorization. |
| **Core Business Services** | User Service    | Manages user registration, login, authentication, and role-based authorization.       |
|                            | Order Service   | Processes orders, including validation, payment, fulfillment, and status updates.     |
|                            | Payment Service | Handles payment processing for orders using various payment gateways.                 |
|                            | Product Service | Provides CRUD operations for product information and inventory management.            |


## Using docker-compose 
These docker-compose commands can be used for various purposes, and here's a breakdown of how they're useful in different situations:

1. **docker-compose up --build -d:**
   - Use case: For development and quick testing where you need to constantly update the code and rebuild containers with the latest changes.
   - Explanation:
     - `up`: Starts all services defined in the docker-compose.yml file.
     - `--build`: Forces a rebuild of all service images before starting them, even if the image already exists.
     - `-d`: Starts the services in detached mode (background).

2. **docker-compose down -v:**
   - Use case: For cleaning up the environment completely after development or before applying significant changes.
   - Explanation:
     - `down`: Stops and removes all containers created by docker-compose.
     - `-v`: Also removes all volumes associated with the services. 

3. **docker-compose up -d:**
   - Use case: For production or staging environments where the code is stable and changes are infrequent.
 
### Additional notes:

You can combine these commands for specific workflows. For example, `docker-compose down -v && docker-compose up --build -d` would first clean up the environment and then rebuild and start all services.

- Consider using environment variables with docker-compose for sensitive information such as passwords or database credentials.

- Use docker-compose volumes instead of bind mounts for persistent data that needs to survive container restarts.

### Docker Compose Services at a Glance

| Service           | Build Context                  | Port | Health Check                              | Depends On (Condition)                                                      | Notes                                      |
|-------------------|--------------------------------|------|-------------------------------------------|-----------------------------------------------------------------------------|--------------------------------------------|
| config-server     | ./config-server                | 8885 | http://localhost:8885/product-service/dev | -                                                                           | Provides configuration properties          |
| service-registry  | ./service-registry             | 8761 | http://localhost:8761/actuator/health     | -                                                                           | Registers and discovers microservices      |
| api-gateway       | ./api-gateway                  | 8090 | -                                         | config-server (healthy), service-registry (healthy), auth-service (healthy) | Routes requests to other services          |
| zipkin-all-in-one | openzipkin/zipkin:latest       | 9411 | -                                         | -                                                                           | Zipkin tracing system                      |
| mysql-db          | mysql:8.0                      | 3306 | mysqladmin ping -h localhost              | -                                                                           | MySQL database                             |
| order-service     | ./order-service                | -    | -                                         | config-server (healthy), service-registry (healthy)                         | 3 replicas, no container_name              |
| product-service   | ./product-service              | 8002 | -                                         | config-server (healthy), service-registry (healthy)                         | Product service                            |
| auth-service      | ./auth-service                 | 8040 | -                                         | mysql-db (healthy), config-server (healthy), service-registry (healthy)     | Authentication service, connects to MySQL  |
| postgres          | postgres:15                    | 5432 | -                                         | -                                                                           | PostgreSQL database                        |
| keycloak          | quay.io/keycloak/keycloak:23.0 | 9090 | -                                         | postgres                                                                    | Keycloak authentication server             |
| user-service	     | ./user-service                 | 8082 | -                                         | config-server (healthy), service-registry (healthy)                         | User Management + Keycloak Admin, Rest API |

**Network:** All services share the `springCloud` network unless otherwise specified.

**Volumes:**

* `postgres_data`: Persistent volume for PostgreSQL database files.
* `mysql_data`: Persistent volume for MySQL database files.

## Business Logic for Microservices System

**User Service:**

* Handles user registration, login, and authentication.
* Stores user data including name, email, address, etc.
* Issues JWT tokens for authorization.
* Validates API requests based on user roles and permissions.

**Order Service:**

* Creates new orders by receiving user ID, product IDs, and quantities.
* Validates product availability and updates inventory levels.
* Calculates total order amount and taxes.
* Communicates with Payment service to process payments.
* Publishes order events to Kafka topics (e.g., "order_created", "order_updated", "order_completed")
* Subscribes to "product_updated" topic to update local product information.

**Product Service:**

* Provides CRUD operations for product information (name, description, price, inventory, etc.)
* Validates product data and enforces business rules.
* Publishes product events to Kafka topics (e.g., "product_created", "product_updated", "product_deleted").

**Payment Service:**

* Handles payment processing for orders using various payment gateways.
* Receives payment requests from Order service with order details and payment information.
* Communicates with payment gateways to process payments securely.
* Updates order status in Order service based on payment success or failure.

**Producer and Consumer Services:**

* Producer:
    * Generates notifications based on events like orders created, processed, or shipped.
    * Publishes notification events to Kafka topics (e.g., "notification_created", "comment_added").
* Consumer:
    * Subscribes to relevant Kafka topics like "notification_created", "comment_added".
    * Consumes events and takes actions like sending email notifications, displaying comments on UI, updating dashboards, etc.

**Additional Services:**

* **Review Service:** Manages product reviews and ratings submitted by users.
* **Recommendation Service:** Recommends products to users based on their past purchases and browsing history.
* **Analytics Service:** Collects and analyzes data from various services to provide insights into user behavior, product performance, and overall system health.

**Business Logic Flow Example:**

1. User logs in to the system using User service.
2. User browses products in Product service and adds desired items to cart.
3. User creates a new order in Order service with selected products.
4. Order service communicates with Product service to validate product availability and update inventory.
5. Order service communicates with Payment service to process payment.
6. Payment service communicates with payment gateway to process payment.
7. Upon successful payment, Order service updates order status and publishes "order_completed" event to Kafka.
8. Producer service consumes "order_completed" event and generates a notification for the user.
9. Consumer service consumes the notification event and sends an email notification to the user.

**Benefits of this microservices architecture:**

* **Scalability:** Each microservice can be independently scaled to meet demand.
* **Resilience:** A failure in one microservice does not affect the entire system.
* **Agility:** Changes can be made to individual microservices without affecting the entire system.
* **Maintainability:** Each microservice has a smaller codebase, making it easier to understand and maintain.

**Next Steps:**

* Define the specific APIs for each microservice.
* Design the data models and storage solutions for each service.
* Choose appropriate technologies for inter-service communication (e.g., REST API, gRPC).
* Implement security measures for user data and API access.
* Develop unit and integration tests for each microservice.
* Deploy the microservices to a cloud platform like AWS, Azure, or GCP.
* Implement monitoring and logging solutions to track system health and performance.

## Order State Machine with Error Handling and Retry Mechanisms

Here's an updated version of the order state machine with error handling, retry mechanisms, and timeouts:

**States:**

* `CREATED`: Initial state after an order is created.
* `VALIDATING`: Order details and product availability are being verified.
* `VALIDATION_FAILED`: Verification failed due to errors.
* `PAYMENT_PENDING`: Payment processing is initiated.
* `PAYMENT_FAILED`: Payment processing failed.
* `FULFILLING`: Order fulfillment process is ongoing.
* `FULFILLMENT_FAILED`: Fulfillment process failed due to errors.
* `FULFILLED`: Order is shipped and completed.
* `CANCELLED`: Order is canceled at any point.

**Transitions:**

| Source State         | Event                   | Target State         | Description                                          | Retry | Timeout             |
|----------------------|-------------------------|----------------------|------------------------------------------------------|-------|---------------------|
| `CREATED`            | `order_created`         | `VALIDATING`         | Order details and product availability are verified. | N/A   | Validation timeout  |
| `VALIDATING`         | `validation_succeeded`  | `PAYMENT_PENDING`    | Successful verification.                             | N/A   | N/A                 |
| `VALIDATING`         | `validation_failed`     | `VALIDATION_FAILED`  | Verification errors occurred.                        | Yes   | N/A                 |
| `VALIDATION_FAILED`  | `retry_validation`      | `VALIDATING`         | Retrying validation after error resolution.          | N/A   | Validation timeout  |
| `PAYMENT_PENDING`    | `payment_initiated`     | `FULFILLING`         | Payment processing initiated.                        | Yes   | Payment timeout     |
| `PAYMENT_PENDING`    | `payment_failed`        | `PAYMENT_FAILED`     | Payment processing failed.                           | N/A   | N/A                 |
| `PAYMENT_FAILED`     | `retry_payment`         | `PAYMENT_PENDING`    | Retrying payment after error resolution.             | N/A   | Payment timeout     |
| `FULFILLING`         | `fulfillment_completed` | `FULFILLED`          | Order successfully shipped.                          | N/A   | N/A                 |
| `FULFILLING`         | `fulfillment_failed`    | `FULFILLMENT_FAILED` | Fulfillment process encountered errors.              | Yes   | Fulfillment timeout |
| `FULFILLMENT_FAILED` | `retry_fulfillment`     | `FULFILLING`         | Retrying fulfillment after error resolution.         | N/A   | Fulfillment timeout |
| Any                  | `order_cancelled`       | `CANCELLED`          | Order is canceled at any point.                      | N/A   | N/A                 |

**Retry Mechanisms:**

* The state machine implements retry mechanisms for validation, payment, and fulfillment processes.
* After a specific timeout, the corresponding event (retry_validation, retry_payment, or retry_fulfillment) is triggered to attempt the operation again.
* The number of retries and the delay between attempts can be configured based on specific requirements.

**Timeouts:**

* Timeouts are defined for validation, payment, and fulfillment processes.
* If the operation doesn't complete within the specified timeout, the state machine transitions to an error state (VALIDATION_FAILED, PAYMENT_FAILED, or FULFILLMENT_FAILED).
* Timeouts can be used to identify potential issues and trigger recovery actions.

**Error Handling:**

* Error states are defined for each potential failure point in the order process.
* The state machine can handle errors gracefully by transitioning to appropriate states and taking corrective actions, such as logging the error, sending notifications, or canceling the order.
* Additional error handling logic can be implemented within each service to handle specific error scenarios.


## Updated Business Logic with State Machine

Here's the updated business logic for order processing with the state machine, error handling, retry mechanisms, and timeouts:

**Order Creation:**

1. User creates an order with selected products.
2. Order service receives the order creation request and validates the order details, product availability, and user information.
3. If validation is successful, the order state transitions to `PAYMENT_PENDING`, and the payment process starts.
4. If validation fails due to errors, the order state transitions to `VALIDATION_FAILED`, and notifications are sent to the user and administrator.
5. After a configured timeout, the `retry_validation` event is triggered to attempt validation again.

**Payment Processing:**

1. Payment service initiates the payment process using the user's payment information.
2. If payment is successful, the order state transitions to `FULFILLING`, and the fulfillment process begins.
3. If payment fails, the order state transitions to `PAYMENT_FAILED`, and notifications are sent to the user and administrator.
4. After a configured timeout, the `retry_payment` event is triggered to attempt payment again.

**Order Fulfillment:**

1. Inventory service checks product availability and reserves the required items.
2. Shipping service processes the shipment and sends the order to the user's address.
3. If fulfillment is successful, the order state transitions to `FULFILLED`, and notifications are sent to the user.
4. If fulfillment fails due to errors (e.g., out of stock items, shipping issues), the order state transitions to `FULFILLMENT_FAILED`, and notifications are sent to the user and administrator.
5. After a configured timeout, the `retry_fulfillment` event is triggered to attempt fulfillment again.

**Order Cancellation:**

1. User or administrator requests to cancel the order.
2. Order service verifies the cancellation request and updates the order state to `CANCELLED`.
3. If payment has already been processed, a refund is initiated.
4. Inventory service updates product availability and releases any reserved items.
5. Notifications are sent to the user and administrator regarding the cancellation.


Based on the limited number of users (less than 100) and the dynamic role change triggered by order status updates, a system-based evaluation approach might be a suitable option for implementing role conversion. Here's how it could work:

1. **Define Order Status Threshold:** Identify a specific order status that serves as the threshold for role change. For example, "In Process Payment" could be the chosen trigger point.
2. **Monitor User Order Status:** Continuously monitor the order status changes for users with the "USER" role.
3. **Automatic Role Change:** When a user's order status reaches the defined threshold (e.g., "In Process Payment"), automatically assign the "ORDER" role to that user.
4. **Payment Completion Reversion:** Once the payment for the relevant order is marked as successful, automatically revert the user's role back to "USER."

Here are some additional suggestions for improvement:

- Define rate limits: Specify the allowed number of requests per user/IP address and the time period for each endpoint (e.g., 100 requests per user per hour).
- Add more responses: Consider including additional response codes for specific error scenarios (e.g., 402 for insufficient funds).
- Versioning: Implement API versioning to manage future updates and changes.

## Future Implementation: Review Service

**Functionalities:**

* **Manage user reviews and ratings for products:**
    * Allow users to submit reviews and ratings with text, images, and videos.
    * Moderate reviews for inappropriate content.
    * Calculate average product rating and display it alongside reviews.
    * Allow users to like or dislike reviews.

**Business Logic:**

1. **Review Submission:**
    * User submits a review with:
        * Rating (e.g., 1-5 stars)
        * Title
        * Content (text, optional images or video)
    * Review service validates the submission:
        * Checks for required fields
        * Checks for inappropriate content using filtering or moderation tools

2. **Review Validation and Storage:**
    * If valid, the review is:
        * Stored in the database with associated product ID, user ID, and metadata
        * Published to a Kafka topic for other services to consume (e.g., Product service for rating updates)
    * If invalid, an error message is displayed to the user, and they are prompted to correct the submission.

3. **Review Moderation:**
    * Review service monitors submitted reviews for inappropriate content:
        * Uses automated content moderation tools or manual review processes
        * Flags or removes reviews that violate community guidelines

4. **Review Display and Interactions:**
    * Approved reviews are displayed on product pages:
        * Title, content, rating, and user information are shown
        * Images or videos are displayed if included
        * Average product rating is calculated and displayed prominently
    * Users can:
        * Like or dislike reviews
        * Leave comments on reviews (also subject to moderation)

**Additional Considerations:**

* **Review Ranking:** Consider implementing algorithms to rank reviews based on factors like helpfulness, recency, or user ratings.
* **Review Reporting:** Allow users to report inappropriate reviews for further moderation.
* **Review Filtering:** Provide options for users to filter reviews based on criteria like rating, date, or keywords.
* **Integration with Other Services:** Consider integrating the Review service with other services for:
    * User profile management
    * Product recommendations
    * Fraud detection
    * Customer support
* **Data Storage and Scalability:** Choose a database solution that can handle large volumes of reviews and ensure scalability for future growth.
* **Security:** Implement security measures to protect user data and prevent unauthorized access or manipulation of reviews.


## Design the data models and storage solutions for each service.

**User Service:**

* **Data model:** Relational model
* **Storage solution:** keycloak + PostgreSQL 
* **Reasons:**
    * Users have structured data (name, email, password, etc.) with well-defined relationships.
    * PostgreSQL is a mature and reliable relational database with strong ACID compliance, ensuring data integrity.
    * Offers efficient querying and filtering capabilities for frequently accessed user data.

**Order Service:**

* **Data model:** Relational model
* **Storage solution:** MySQL
* **Reasons:**
    * Orders have structured data (customer ID, products ordered, order status, etc.) with relationships to User and Product services.
    * MySQL provides efficient joins and transactions for managing order data and related information.

**Product Service:**

* **Data model:** Relational model
* **Storage solution:** MySQL (separate database from Order services)
* **Reasons:**
    * Products have structured data (product name, description, price, inventory, etc.) with potential relationships to other services (e.g., reviews, recommendations).
    * MySQL offers efficient searching and filtering for product information.
    * Separating it from other services improves performance and scalability.

**Payment Service:**

* **Data model:** Relational model
* **Storage solution:** PostgreSQL (separate database from User services)
* **Reasons:**
    * Payment data requires strong ACID compliance and complex transactions.
    * PostgreSQL offers advanced features like triggers and stored procedures for managing financial transactions.
    * Its support for JSON data types can be beneficial for storing payment details.


## Resources for documentation
For further reference, please consider the following sections:

* **Learn the basics of Spring Cloud Config:**
    * [Docker’s health check and Spring Boot apps - how to control containers startup order in docker-compose](https://medium.com/@aleksanderkolata/docker-spring-boot-and-containers-startup-order-39230e5352a4)

* **Keycloak Realm Exports:**
    * [Users and Client Secrets in Keycloak Realm Exports](https://candrews.integralblue.com/2021/09/users-and-client-secrets-in-keycloak-realm-exports/)  Edit the Administrative Interface’s Realm Export Json
    * [Keycloak in Docker #5 – How to export a realm with users and secrets](https://keepgrowing.in/tools/keycloak-in-docker-5-how-to-export-a-realm-with-users-and-secrets/)

* **Examples projects**
    * [Blog Application](https://github.com/cokutan/blogapplication/tree/develop) (Config Server + Eureka Server + Gateway + App + Mongodb)