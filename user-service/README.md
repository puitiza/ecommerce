# User Service

The User Service manages user registration, login, and role-based authorization using Keycloak for OAuth2 and JWT.

## Key Features
- **User Management**: Handles registration, login, and profile updates.
- **Role-Based Authorization**: Supports `ADMIN` and `USER` roles.
- **Keycloak Integration**: Uses OAuth2 for secure authentication.

## Concepts
| Name              | Description                                                              |
|-------------------|--------------------------------------------------------------------------|
| Roles             | User types (e.g., `ADMIN`, `USER`) for access control.                   |
| User Role Mapping | Associates roles with users, stored in JWT tokens.                       |
| Composite Roles   | Roles inheriting other roles (e.g., `superuser` includes `sales-admin`). |
| Groups            | Manage user sets with shared attributes and roles.                       |
| Realms            | Isolated user management scopes (e.g., `ecommerce` realm).               |
| Clients           | Entities requesting authentication from Keycloak.                        |
| Client Role       | Client-specific roles for fine-grained access control.                   |
| Client Scopes     | Shared protocol mappers and role mappings for clients.                   |

## Key Endpoints
- `POST /users/signup`: Register a new user (public).
- `POST /users/login`: Authenticate and obtain JWT (public).
- `GET /users/{id}`: Retrieve user details (ADMIN only).
- `GET /users/me`: Retrieve own profile (USER).
- `PUT /users/me`: Update own profile (USER).

## Production Considerations
- **Storage**: Uses PostgreSQL for user data, integrated with Keycloak.
- **Secrets**: Store Keycloak credentials in Azure Key Vault (see [docs/production-setup.md](../config/docs/production-setup.md)).
- **Tracing**: Use Azure Application Insights in production.

## Multi-Module Integration
Shares DTOs and exceptions with other services via the `common` module. See [docs/multi-module.md](../config/docs/multi-module.md).

## Key Resources for Documentation

**Keycloak Integration and Security:**

* **Securing Spring Boot Applications with Keycloak:**
    * [Secure Spring Boot REST API using Keycloak](https://www.tutorialsbuddy.com/keycloak-secure-spring-boot-rest-api#gsc.tab=0)
    * [Securing Spring Boot Applications with Keycloak and Spring Security](https://www.atlantbh.com/securing-spring-boot-applications-with-keycloak-and-spring-security/)
    * [Using Keycloak with Spring Boot 3.0](https://medium.com/geekculture/using-keycloak-with-spring-boot-3-0-376fa9f60e0b)
    * [Set up Keycloak in Spring Boot using the Keycloak Admin API](https://gauthier-cassany.com/posts/spring-boot-keycloak-admin-api)

* **Keycloak User Management:**
    * [Search Users With Keycloak in Java](https://www.baeldung.com/java-keycloak-search-users)
    * [Keycloak Admin Client with Spring Boot](https://codersee.com/how-to-set-up-keycloak-admin-client-with-spring-boot-and-kotlin/)

* **Keycloak Realm Management:**
    * [How to create a new realm with the Keycloak REST API](https://suedbroecker.net/2020/08/04/how-to-create-a-new-realm-with-the-keycloak-rest-api/)

**Keycloak General Documentation:**

* [Keycloak features and concepts](https://www.keycloak.org/docs/latest/server_admin/#keycloak-features-and-concepts)
* [Supported APIs](https://www.npmjs.com/package/@keycloak/keycloak-admin-client)
* [Official Documentation Keycloak](https://www.keycloak.org/documentation.html)

**Code Examples:**

* [KeycloakAdminClientExample.java](https://gist.github.com/thomasdarimont/a19cf78a4cff3b87173a84b)

