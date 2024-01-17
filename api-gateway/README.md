# Api-Gateway

**Overview**

This project implements a Spring Cloud Gateway to handle HTTP traffic between microservices, providing security features
like JWT, CORS, rate limiting, and token relay.

**Key Features**

* **JSON Web Tokens (JWT):** Securely transmits information using a signed token structure.
  A JWT is a string that
  consists of three parts: a header, a payload, and a signature.

### Configuration

- **CORS** Enables cross-origin requests from authorized domains.

  - ***Keycloak token:*** Enables internal API gateway calls to other resources on behalf of the user.
  `"allowed-origins": ["http://localhost:8090"]`
  - ***CorsWebFilter:*** Enables Swagger UI on `localhost:9090` to access the API gateway directly.

  ```yaml
  spring:
    cloud:
      gateway:
        globalcors:
          corsConfigurations:
            '[/**]':
              allowedOrigins:
                - "http://localhost:8090"  # Example allowed origin
              allowedMethods:
                - GET
                - POST
                - PUT
                - DELETE
                - OPTIONS
  ```

- **Rate Limiting** Controls the rate of incoming requests to protect against overload.

  ```yaml
  spring:
    cloud:
      gateway:
        routes:
          - id: rate-limited-route
            uri: https://example.org
            filters:
              - name: RequestRateLimiter #this is a default, but you can also define your own (optional) implementation of the rate limiter as a spring bean.
                args:
                  key-resolver: "#{@hostAddressKeyResolver}" #An (optional) key resolver defines the key for limiting requests.
                  redis-rate-limiter.replenishRate: 10 #Defines how many requests per second to allow (without any dropped requests). This is the rate at which the token bucket is filled.
                  redis-rate-limiter.burstCapacity: 20 #The maximum number of requests a user is allowed in a single second (without any dropped requests). This is the number of tokens the token bucket can hold. Setting this value to zero blocks all requests.
                  redis-rate-limiter.requestedTokens: 1 #Defines how many tokens a request costs. 
  ```

- **Token Relay** Forwards authentication tokens to downstream services for secure access.

  ```yaml
  spring:
    cloud:
      gateway:
        default-filters:
          - TokenRelay=
  ```

  **Additional Information**

    * **Client Configuration:**
      ```groovy
      implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
      ```
      ```yaml
      client:
        provider:
          keycloak:
            issuer-uri: ${KEYCLOAK_SERVER_URL:http://localhost:9090}/realms/ecommerce
            user-name-attribute: preferred_username
        registration:
          ecommerce:
            provider: keycloak
            client-id: api-gateway-client
            client-secret: fB6qaEazuI3oZJYHB1lu91Py6FbKP1m6
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
      ```

## Resources for documentation

* **Learn the basics of Spring Cloud Gateway:**
    * [Spring Cloud Gateway Security with JWT](https://medium.com/@rajithgama/spring-cloud-gateway-security-with-jwt-23045ba59b8a):
      Explore JWT implementation for Gateway security.
    * [API Gateway in Spring Boot](https://medium.com/@ankithahjpgowda/api-gateway-in-spring-boot-3ea804003021):
      Understand the basics of building an API gateway with Spring Boot.
* **Integrate OpenAPI for documentation:**
    * [Central Swagger in Spring Cloud Gateway](https://medium.com/@oguz.topal/central-swagger-in-spring-cloud-gateway-697a1c37b03d):
      Manage Swagger documentation centrally for your gateway.
    * [Swagger(OpenAPI Specification 3) Integration with Spring Cloud Gateway —
      Part 2](https://medium.com/@pubuduc.14/swagger-openapi-specification-3-integration-with-spring-cloud-gateway-part-2-1d670d4ab69a):
      Part 2 of a detailed guide on Swagger integration with the gateway.
    * [Microservices with Spring Boot 3 and Spring Cloud](https://piotrminkowski.com/2023/03/13/microservices-with-spring-boot-3-and-spring-cloud/)
* **Integrate Oauth2 for secure access:**
    * [Spring Cloud Gateway OAuth2 Security with Keycloak,
      JWT Tokens and securing it with HTTPS (SSL)](https://blog.devops.dev/spring-cloud-gateway-oauth2-security-with-keycloak-jwt-tokens-and-securing-it-with-https-ssl-2166d8009531):
      Secure your gateway using Keycloak, JWT, and HTTPS.
    * [Documenting OAuth2 secured Spring Boot Microservices with Swagger 3 (OpenAPI 3.0)](https://medium.com/@tobintom/documenting-oauth2-secured-spring-boot-microservices-with-swagger-3-openapi-3-0-166618ea1f5):
      Learn how to document Oauth2-secured microservices with Swagger.
* **Integrate Keycloak for authorization server:**
    * [Connecting Keycloak with Postgres database](https://stackoverflow.com/questions/75410699/connecting-keycloak-with-postgres-database):
      list complete of set env for docker-compose.yml
    * [Configure Keycloak to use a Postgres database](https://www.youtube.com/watch?v=7404ir5oq4Q&t=335s): This video
      you could help
    * [All Configuration](https://www.keycloak.org/server/all-config?options-filter=all): all configuration according
      official website
* **Logging Spring Cloud Gateway**
    * [Log All response and request spring webflux](https://stackoverflow.com/questions/76045158/log-all-response-and-request-spring-webflux)
* **Rate Limit**
    * [Resilience Retry, Circuit Breaking and Rate Limiting](https://andifalk.gitbook.io/spring-cloud-gateway-workshop/hands-on-labs/lab2)
    * [Implement Rate Limiting in Spring Cloud Gateway with Redis](https://medium.com/@htyesilyurt/implement-rate-limiting-in-spring-cloud-gateway-with-redis-7b71c8dd53a3):
      Rate limiting is a technique used to control the rate at which requests are made to a network
    * [Rate Limiter using Spring Cloud Gateway and Redis](https://www.youtube.com/watch?v=0LoqPg6h6wc&ab_channel=TechPrimers)
      Example from YouTube
* **Token Relay**
    * [Official Token Relay](https://cloud.spring.io/spring-cloud-static/spring-cloud-security/2.1.3.RELEASE/single/spring-cloud-security.html#_token_relay):
      A Token Relay is where an OAuth2 consumer acts as a Client and forwards the incoming token to outgoing resource
      requests.
      The consumer can be a pure Client (like an SSO application) or a Resource Server.

* **Examples projects:**
    * [Spring Cloud Gateway with OpenID Connect and Token Relay](https://github.com/timtebeek/spring-security-samples/blob/main/spring-cloud-gateway-oidc-tokenrelay/README.adoc):
      When combined with Spring Security 5.2+ and an OpenID Provider such as Keycloak, one can rapidly set up a secure
      gateway for OAuth2 resource servers.
    * [Spring-cloud-gateway-request-rate-limiting](https://github.com/ivvve/code-examples/tree/master/spring-cloud-gateway-request-rate-limiting):
      Api Server + Gateway Server