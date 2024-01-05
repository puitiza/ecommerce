# User-Service API

The following was discovered as part of building this project:

## Concepts

KEYCLOAK implements almost all standard identity and access management (IAM) protocols like OAuth 2.0, OpenID, and SAML.
So we can use one of these protocols to connect with Keycloak

| Name              | Description                                                                                                                                                                                                                              |
|-------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Roles             | Identify a user type or category (e.g., admin, user, manager, employee). Applications often grant access and permissions based on roles for easier management.                                                                           |
| User Role Mapping | Defines the association between a role and a user. A user can have zero or more roles. This information can be stored in tokens and assertions for access control by applications.                                                       |
| Composite Roles   | Roles that can be associated with other roles (e.g., a "superuser" role associated with "sales-admin" and "order-entry-admin" roles). A user assigned to the "superuser" role automatically inherits the other two roles.                |
| Groups            | Manage sets of users with defined attributes. Roles can be mapped to groups, and users joining a group inherit its attributes and role mappings.                                                                                         |
| Realms            | Manage users, credentials, roles, and groups. A user belongs to and logs into a specific realm. Realms are isolated from each other and can only manage their own users and authentication.                                              |
| Clients           | Are entities that can request Keycloak to authenticate a user                                                                                                                                                                            |
| Client role       | Clients can define roles that are specific to them. This is basically a role namespace dedicated to the client. More information [link](https://www.keycloak.org/docs/latest/server_admin/#assigning-permissions-using-roles-and-groups) |
| Client scopes     | When a client is registered, you must define protocol mappers and role scope mappings for that client. It is often useful to store a client scope, to make creating new clients easier by sharing some common settings                   |

## Key Resources for Documentation

**Keycloak Integration and Security:**

* **Securing Spring Boot Applications with Keycloak:**
    * [Secure Spring Boot REST API using Keycloak](https://www.tutorialsbuddy.com/keycloak-secure-spring-boot-rest-api#gsc.tab=0)
    * [Securing Spring Boot Applications with Keycloak and Spring Security](https://www.atlantbh.com/securing-spring-boot-applications-with-keycloak-and-spring-security/)
    * [Using Keycloak with Spring Boot 3.0](https://medium.com/geekculture/using-keycloak-with-spring-boot-3-0-376fa9f60e0b)
    * [Set up Keycloak in Spring Boot using the Keycloak Admin API](https://gauthier-cassany.com/posts/spring-boot-keycloak-admin-api)

* **Keycloak User Management:**
    * [Search Users With Keycloak in Java](https://www.baeldung.com/java-keycloak-search-users)
    * [How to Set Up Keycloak Admin Client with Spring Boot and Kotlin?](https://codersee.com/how-to-set-up-keycloak-admin-client-with-spring-boot-and-kotlin/)

* **Keycloak Realm Management:**
    * [How to create a new realm with the Keycloak REST API](https://suedbroecker.net/2020/08/04/how-to-create-a-new-realm-with-the-keycloak-rest-api/)

**Keycloak General Documentation:**

* [Keycloak features and concepts](https://www.keycloak.org/docs/latest/server_admin/#keycloak-features-and-concepts)
* [Supported APIs](https://www.npmjs.com/package/@keycloak/keycloak-admin-client)
* [Official Documentation Keycloak](https://www.keycloak.org/documentation.html)

**Code Examples:**

* [KeycloakAdminClientExample.java](https://gist.github.com/thomasdarimont/a19cf78a4cff3b87173a84b)



