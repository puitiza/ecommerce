# Api-Gateway

Spring Cloud Gateway provides a powerful way to handle HTTP traffic between microservices. It also provides several
mechanisms for securing the gateway, including JWT.

**JSON Web Tokens (JWT)** are a popular way of securely transmitting information between parties. A JWT is a string that
consists of three parts: a header, a payload, and a signature

Therefore, your approach is correct. You've utilized both mechanisms to enable CORS for different scenarios:
- **Keycloak token:** Enables internal API gateway calls to other resources on behalf of the user.
  `"allowed-origins": ["http://localhost:8090"]`
- **CorsWebFilter:** Enables Swagger UI on `localhost:9090` to access the API gateway directly.

This approach provides flexibility and ensures both internal and external communication is handled correctly with respect to CORS.

The rate limiter defines the following properties:
- redis-rate-limiter.replenishRate: Defines how many requests per second to allow (without any dropped requests). This is the rate at which the token bucket is filled.
- redis-rate-limiter.burstCapacity: The maximum number of requests a user is allowed in a single second (without any dropped requests). This is the number of tokens the token bucket can hold. Setting this value to zero blocks all requests.
- redis-rate-limiter.requestedTokens: Defines how many tokens a request costs. This is the number of tokens taken from the bucket for each request and defaults to 1.
- rate-limiter: You can also define your own (optional) implementation of the rate limiter as spring bean.
- key-resolver: An (optional) key resolver defines the key for limiting requests.


## Resources for documentation

* **Learn the basics of Spring Cloud Gateway:**
  * [Spring Cloud Gateway Security with JWT](https://medium.com/@rajithgama/spring-cloud-gateway-security-with-jwt-23045ba59b8a): Explore JWT implementation for Gateway security.
  * [API Gateway in Spring Boot](https://medium.com/@ankithahjpgowda/api-gateway-in-spring-boot-3ea804003021): Understand the basics of building an API gateway with Spring Boot.
* **Integrate OpenAPI for documentation:**
  * [Central Swagger in Spring Cloud Gateway](https://medium.com/@oguz.topal/central-swagger-in-spring-cloud-gateway-697a1c37b03d): Manage Swagger documentation centrally for your gateway.
  * [Swagger(OpenAPI Specification 3) Integration with Spring Cloud Gateway — Part 2](https://medium.com/@pubuduc.14/swagger-openapi-specification-3-integration-with-spring-cloud-gateway-part-2-1d670d4ab69a): Part 2 of a detailed guide on Swagger integration with the gateway.
  * [Microservices with Spring Boot 3 and Spring Cloud](https://piotrminkowski.com/2023/03/13/microservices-with-spring-boot-3-and-spring-cloud/)
* **Integrate Oauth2 for secure access:**
  * [Spring Cloud Gateway OAuth2 Security with Keycloak, JWT Tokens and securing it with HTTPS (SSL)](https://blog.devops.dev/spring-cloud-gateway-oauth2-security-with-keycloak-jwt-tokens-and-securing-it-with-https-ssl-2166d8009531): Secure your gateway using Keycloak, JWT, and HTTPS.
  * [Documenting OAuth2 secured Spring Boot Microservices with Swagger 3 (OpenAPI 3.0)](https://medium.com/@tobintom/documenting-oauth2-secured-spring-boot-microservices-with-swagger-3-openapi-3-0-166618ea1f5): Learn how to document Oauth2-secured microservices with Swagger.
* **Integrate Keycloak for authorization server:**
  * [Connecting Keycloak with Postgres database](https://stackoverflow.com/questions/75410699/connecting-keycloak-with-postgres-database): list complete of set env for docker-compose.yml
  * [Configure Keycloak to use a Postgres database](https://www.youtube.com/watch?v=7404ir5oq4Q&t=335s): This video you could help
  * [All Configuration](https://www.keycloak.org/server/all-config?options-filter=all): all configuration according official website
* **Logging Spring Cloud Gateway**
  * [Log All response and request spring webflux](https://stackoverflow.com/questions/76045158/log-all-response-and-request-spring-webflux)
* **Rate Limit**
  * [Resilience Retry, Circuit Breaking and Rate Limiting](https://andifalk.gitbook.io/spring-cloud-gateway-workshop/hands-on-labs/lab2)
  * [Implement Rate Limiting in Spring Cloud Gateway with Redis](https://medium.com/@htyesilyurt/implement-rate-limiting-in-spring-cloud-gateway-with-redis-7b71c8dd53a3) Rate limiting is a technique used to control the rate at which requests are made to a network
  * [Rate Limiter using Spring Cloud Gateway and Redis](https://www.youtube.com/watch?v=0LoqPg6h6wc&ab_channel=TechPrimers) Example from YouTube
* **Examples projects:**
  * [Spring Cloud Gateway with OpenID Connect and Token Relay](https://github.com/timtebeek/spring-security-samples/blob/main/spring-cloud-gateway-oidc-tokenrelay/README.adoc): When combined with Spring Security 5.2+ and an OpenID Provider such as Keycloak, one can rapidly set up a secure gateway for OAuth2 resource servers.
  * [Spring-cloud-gateway-request-rate-limiting](https://github.com/ivvve/code-examples/tree/master/spring-cloud-gateway-request-rate-limiting) Api Server + Gateway Server
