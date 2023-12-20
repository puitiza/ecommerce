# ecommerce

This is a personal project in order to understand better microservices,
and it split by section and commits.

[![Build Status](https://travis-ci.org/joemccann/dillinger.svg?branch=master)](https://travis-ci.org/joemccann/dillinger)
[![Build Status](https://github.com/javiertuya/samples-test-spring/actions/workflows/build.yml/badge.svg)](https://github.com/javiertuya/samples-test-spring/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=my%3Asamples-test-spring&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=my%3Asamples-test-spring)
[![Javadoc](https://img.shields.io/badge/%20-javadoc-blue)](https://javiertuya.github.io/samples-test-spring/)

<img src="https://github.com/puitiza/ecommerce/blob/main/images/Architecture%20Software%20final.png?raw=true">     


# About the project

<ul style="list-style-type:disc">
These stages include checking out code, performing quality code analysis, building and pushing Docker images,
and deploying the services on a Kubernetes cluster.

The architecture uses several services and tools for different purposes:

  <li>This project is based Spring Boot 3.0 Microservices with the usage of Docker and Kubernetes</li>
  <li>User can register and login through auth service by user role (ADMIN or USER) through api gateway</li>
  <li>User can send any request to relevant service through api gateway with its bearer token</li>
</ul>

7 services whose name are shown below have been devised within the scope of this project.

- Config Server
- Eureka Server
- API Gateway
- Auth Service
- Order Service
- Payment Service
- Product Service
- User Service as Producer
- Comment Service as Consumer

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

Consider using environment variables with docker-compose for sensitive information such as passwords or database credentials.
Use docker-compose volumes instead of bind mounts for persistent data that needs to survive container restarts.

### Docker Compose Services at a Glance

| Service           | Build Context                  | Port | Health Check                              | Depends On (Condition)                                                      | Notes                                     |
|-------------------|--------------------------------|------|-------------------------------------------|-----------------------------------------------------------------------------|-------------------------------------------|
| config-server     | ./config-server                | 8885 | http://localhost:8885/product-service/dev | -                                                                           | Provides configuration properties         |
| service-registry  | ./service-registry             | 8761 | http://localhost:8761/actuator/health     | -                                                                           | Registers and discovers microservices     |
| api-gateway       | ./api-gateway                  | 8090 | -                                         | config-server (healthy), service-registry (healthy), auth-service (healthy) | Routes requests to other services         |
| zipkin-all-in-one | openzipkin/zipkin:latest       | 9411 | -                                         | -                                                                           | Zipkin tracing system                     |
| mysql-db          | mysql:8.0                      | 3306 | mysqladmin ping -h localhost              | -                                                                           | MySQL database                            |
| order-service     | ./order-service	               | -    | -                                         | config-server (healthy), service-registry (healthy)	                        | 3 replicas, no container_name             |
| product-service   | ./product-service              | 8002 | -                                         | config-server (healthy), service-registry (healthy)                         | Product service                           |
| auth-service      | ./auth-service                 | 8040 | -                                         | mysql-db (healthy), config-server (healthy), service-registry (healthy)     | Authentication service, connects to MySQL |
| postgres          | postgres:15                    | 5432 | -                                         | -                                                                           | PostgreSQL database                       |
| keycloak          | quay.io/keycloak/keycloak:23.0 | 9090 | -                                         | postgres                                                                    | Keycloak authentication server            |

**Network:** All services share the `springCloud` network unless otherwise specified.

**Volumes:**

* `postgres_data`: Persistent volume for PostgreSQL database files.
* `mysql_data`: Persistent volume for MySQL database files.


## Resources for documentation
For further reference, please consider the following sections:

* **Learn the basics of Spring Cloud Config:**
    * [Docker’s health check and Spring Boot apps - how to control containers startup order in docker-compose](https://medium.com/@aleksanderkolata/docker-spring-boot-and-containers-startup-order-39230e5352a4)


* **Examples projects**
    * [Blog Application](https://github.com/cokutan/blogapplication/tree/develop) (Config Server + Eureka Server + Gateway + App + Mongodb)

# CI/CD Pipeline Stages

## Stage 1: Checkout

- Checkout the source code from the GitHub repository.
- Configure the MySQL database and User Service.

## Stage 2: Quality Code

- Conduct static code analysis using SonarQube.
- Verify the Order Service, Product Service, and OpenAPI Specifications.

## Stage 3: Build and Push

- Set up OAuth for authorization.
- Build the MySQL, Payment Service, Notification Service, and Users docker images.
- Push the images to Docker Hub.

## Stage 4: Deploy

- Deploy the HELM Chart to update the k8s manifest.
- Utilize Kafka for event-driven architecture.
- Set up Config Server, Discovery Server (Eureka), and Kubernetes clusters.

## Stage 5: CD Sync

- Sync the GitHub Actions workflow with Argo CD for seamless CI/CD pipeline.

## Stage 6: Metrics Monitoring

- Integrate the Metrics Server for Kubernetes metrics.
- Utilize Distributed Trace (Zipkin), Logs (fluentd), and Visualization Events (Kadeck) for real-time monitoring.

## Stage 7: Dashboard

- Deploy the ELK Stack (Elastic, Kibana) for log management and monitoring.
- Utilize the Lens IDE and Grafana Dashboard for Kubernetes monitoring.

## Stage 8: Database Service

- Implement the MongoDB Database Service for data persistence.

## Stage 9: Microservices Deployment

- Deploy the Microservices (DEV, UAT, and PROD) on Kubernetes clusters.

## Stage 10: Final Deployment

- Developers commit code to the DEV cluster.
- Code is deployed to the UAT cluster for further testing.
- Once testing is complete, the code is deployed to the PROD cluster.

## Stage 11: Monitoring System

- Continuously monitor the application using the integrated monitoring tools and systems.
