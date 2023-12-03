# ecommerce

This is a personal project in order to understand better microservices,
and it split by section and commits.

[![Build Status](https://travis-ci.org/joemccann/dillinger.svg?branch=master)](https://travis-ci.org/joemccann/dillinger)
[![Build Status](https://github.com/javiertuya/samples-test-spring/actions/workflows/build.yml/badge.svg)](https://github.com/javiertuya/samples-test-spring/actions/workflows/build.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=my%3Asamples-test-spring&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=my%3Asamples-test-spring)
[![Javadoc](https://img.shields.io/badge/%20-javadoc-blue)](https://javiertuya.github.io/samples-test-spring/)

<img src="https://github.com/puitiza/ecommerce/blob/main/images/Architecture%20Software%20final.png">     


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

- Sync the Github Actions workflow with Argo CD for seamless CI/CD pipeline.

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
