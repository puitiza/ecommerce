# Eureka Service Registry

The Eureka Discovery Server enables microservices to register and discover each other dynamically, eliminating hard-coded IP addresses and simplifying communication.

## Key Features
- **Service Registration**: Microservices register with Eureka (name, host, port).
- **Service Discovery**: Services query Eureka to find other registered services.
- **High Availability**: Supports clustering for fault tolerance.
- **Load Balancing**: Integrates with load balancers for traffic distribution.

## Getting Started
1. Start the Eureka server (configured in Spring Boot).
2. Register microservices using `@EnableEurekaClient` or equivalent.
3. Use service names for communication (e.g., `http://order-service/orders`).

### Examples
- Registering `product-service`:
  ```java
  @EnableEurekaClient
  @SpringBootApplication
  public class ProductServiceApplication {
      // ...
  }
  ```
- Calling `order-service`:
  ```java
  String url = "http://order-service/orders";
  RestTemplate restTemplate = new RestTemplate();
  List<Order> orders = restTemplate.getForObject(url, List.class);
  ```

## Production Considerations
- **Security**: Secure Eureka with authentication and restrict access to trusted services.
- **Kubernetes**: Deploy Eureka as a Kubernetes service with ClusterIP (see [docs/production-setup.md](../config/docs/production-setup.md)).

## Multi-Module Integration
Uses shared utilities from the `common` module for consistent service discovery logic. See [docs/multi-module.md](../config/docs/multi-module.md).

## Resources
- [Netflix Eureka Documentation](https://github.com/Netflix/eureka)




