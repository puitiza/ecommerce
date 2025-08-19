# Contributing to E-commerce Microservices

Thank you for your interest in contributing to the e-commerce microservices platform! This project is a showcase of
microservices architecture using **Spring Boot 3.0**, **Docker**, **Kubernetes**, and other modern technologies. We
welcome contributions from the community to improve features, fix bugs, or enhance documentation. This guide outlines
the process for contributing effectively.

---

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [How to Contribute](#how-to-contribute)
    - [Reporting Bugs](#reporting-bugs)
    - [Suggesting Features](#suggesting-features)
    - [Submitting Code Changes](#submitting-code-changes)
3. [Development Setup](#development-setup)
4. [Coding Standards](#coding-standards)
5. [Pull Request Guidelines](#pull-request-guidelines)
6. [Testing Guidelines](#testing-guidelines)
7. [Documentation Guidelines](#documentation-guidelines)
8. [Resources](#resources)

---

## Code of Conduct

We are committed to fostering an inclusive and respectful community. By contributing, you agree to:

- Be respectful and professional in all interactions.
- Avoid discriminatory or offensive language.
- Collaborate constructively and provide helpful feedback.

Please report any unacceptable behavior to [your-email@example.com].

---

## How to Contribute

There are several ways to contribute to the project, including reporting bugs, suggesting features, submitting code
changes, or improving documentation.

### Reporting Bugs

1. **Check Existing Issues**: Search the [GitHub Issues](https://github.com/puitiza/ecommerce/issues) to avoid
   duplicates.
2. **Open an Issue**:
    - Use the "Bug Report" template (if available).
    - Provide a clear title (e.g., "[Order Service] NullPointerException in OrderEventProcessor").
    - Include:
        - Steps to reproduce the bug.
        - Expected vs. actual behavior.
        - Environment details (e.g., Java version, Docker version).
        - Relevant logs or screenshots.
3. **Label the Issue**: Use labels like `bug`, `service:order-service`, or `priority:high`.

### Suggesting Features

1. **Check Existing Feature Requests**: Review [GitHub Issues](https://github.com/puitiza/ecommerce/issues) for similar
   suggestions.
2. **Open a Feature Request**:
    - Use the "Feature Request" template (if available).
    - Describe the feature, its use case, and potential benefits.
    - Specify the affected service(s) (e.g., `product-service`, `api-gateway`).
3. **Engage in Discussion**: Respond to feedback to refine the proposal.

### Submitting Code Changes

1. **Fork the Repository**:
   ```bash
   git clone https://github.com/<your-username>/ecommerce.git
   cd ecommerce
   ```
2. **Create a Feature Branch**:
   ```bash
   git checkout -b feature/<feature-name>
   ```
   Example: `feature/add-review-service`
3. **Make Changes**: Follow [Coding Standards](#coding-standards) and [Testing Guidelines](#testing-guidelines).
4. **Commit Changes**:
    - Use clear, concise commit messages (e.g., "Add order validation for negative quantities in OrderService").
    - Reference related issues (e.g., "Fixes #123").
5. **Push to Your Fork**:
   ```bash
   git push origin feature/<feature-name>
   ```
6. **Submit a Pull Request**: See [Pull Request Guidelines](#pull-request-guidelines).

---

## Development Setup

To set up the project for development, follow the instructions in the [main README](/README.md#getting-started)
and [Developer Workflow](config/docs/developer-workflow.md). Key steps:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/puitiza/ecommerce.git
   cd ecommerce
   ```
2. **Install Prerequisites**: Java 21, Gradle 8.0+, Docker, Docker Compose.
3. **Start the Stack**:
   ```bash
   docker-compose up --build -d
   ```
4. **Run Tests**:
   ```bash
   ./gradlew test
   ```

For advanced workflows (e.g., hot reloading, debugging), refer
to [docs/developer-workflow.md](config/docs/developer-workflow.md).

---

## Coding Standards

To ensure code quality and consistency:

- **Language**: Use Java 21 with Spring Boot 3.0 conventions.
- **Style Guide**:
    - Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
    - Use Checkstyle (configured in `checkstyle.xml`) to enforce formatting.
    - Run `./gradlew checkstyleMain` before committing.
- **Naming Conventions**:
    - Classes: `UpperCamelCase` (e.g., `OrderService`).
    - Methods/Variables: `camelCase` (e.g., `createOrder`).
    - Constants: `UPPER_SNAKE_CASE` (e.g., `ORDER_CREATED_TOPIC`).
- **Code Organization**:
    - Place DTOs, exceptions, and utilities in the `share-library` module for reuse.
    - Follow package structure: `com.example.ecommerce.<service-name>.<layer>` (e.g., `controller`, `service`,
      `repository`).
- **Comments**: Use Javadoc for public methods and classes. Example:
  ``` java
  /**
   * Creates an order with the specified items and user ID.
   * @param orderRequest the order details
   * @param userId the ID of the user
   * @return the created order
   * @throws OrderValidationException if validation fails
   */
  public Order createOrder(OrderRequest orderRequest, String userId) { ... }
  ```

---

## Pull Request Guidelines

1. **Create a Pull Request**:
    - Target the `main` branch.
    - Use a descriptive title (e.g., "Add payment retry logic to PaymentService").
    - Reference related issues (e.g., "Closes #123").
2. **Description**:
    - Summarize the changes and their purpose.
    - List affected services (e.g., `order-service`, `api-gateway`).
    - Include testing details (e.g., "Added unit tests for OrderService").
3. **Checklist**:
    - [ ] Code follows [Coding Standards](#coding-standards).
    - [ ] Tests pass (`./gradlew test`).
    - [ ] Checkstyle passes (`./gradlew checkstyleMain`).
    - [ ] Documentation is updated (e.g., README, Javadoc).
4. **CI/CD**: Ensure the GitHub Actions pipeline (`build.yml`) passes.
5. **Review Process**:
    - Respond to feedback promptly.
    - Make requested changes and update the PR.

---

## Testing Guidelines

All code changes must include tests to maintain reliability:

- **Unit Tests**:
    - Use JUnit 5 and Mockito.
    - Test business logic in isolation (e.g., `OrderService` state transitions).
    - Place in `src/test/java`.
- **Integration Tests**:
    - Use Testcontainers for MySQL and Kafka.
    - Test service interactions (e.g., `OrderService` with `PaymentService`).
    - Place in `src/integrationTest/java`.
- **Run Tests**:
  ```bash
  ./gradlew test
  ./gradlew integrationTest
  ```
- **Coverage**: Aim for >80% code coverage (check with `./gradlew jacocoTestReport`).

For load testing, see [docs/developer-workflow.md](config/docs/developer-workflow.md#load-tests-with-jmeter).

---

## Documentation Guidelines

- **Update READMEs**: Modify service-specific READMEs (e.g., `order-service/README.md`) for new features or changes.
- **Javadoc**: Add Javadoc for public APIs in new or modified code.
- **Changelog**: Update [docs/CHANGELOG.md](config/docs/CHANGELOG.md) for significant changes.
- **Format**: Use Markdown with clear headings, code blocks, and callouts (`> **Note**`).

Example changelog entry:

```markdown
## [0.2.0] - 2025-08-19

### Added

- Retry logic for payment processing in `payment-service` (#123).

### Fixed

- NullPointerException in `OrderEventProcessor` (#124).
```

---

## Resources

- **GitHub Issues**: [Issue Tracker](https://github.com/puitiza/ecommerce/issues)
- **Spring Boot**: [Official Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- **Checkstyle**: [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- **Testcontainers**: [Official Documentation](https://www.testcontainers.org/)
- **Git Commit Messages**: [Conventional Commits](https://www.conventionalcommits.org/)

---

### Key Features of CONTRIBUTING.md

- **Purpose**: Guides contributors on how to report bugs, suggest features, submit code, and maintain quality.
- **Structure**: Includes a table of contents, code of conduct, and clear steps for contributing.
- **Practical Instructions**: Provides commands for forking, branching, and testing, with references to
  `developer-workflow.md`.
- **Standards**: Enforces coding and testing guidelines to ensure consistency.
- **Modern Conventions**: Uses callouts, checklists, and links to external resources.
