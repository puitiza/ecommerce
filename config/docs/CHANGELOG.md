# Changelog

This changelog tracks significant changes to the e-commerce microservices platform, including new features, bug fixes,
and breaking changes. All notable updates are documented here, following
the [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) format and adhering
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## Table of Contents

1. [Unreleased](#unreleased)
2. [0.2.0] - 2025-08-19
3. [0.1.0] - 2025-06-01

---

## Unreleased

### Added

- Planned review service with MongoDB for product reviews and ratings.
- Planned recommendation service using Neo4j for product suggestions.

### Changed

- N/A

### Fixed

- N/A

---

## [0.2.0] - 2025-08-19

### Added

- Retry logic for payment processing in `payment-service` (#123).
- Kafka Dead Letter Topic handling for `order-service` (#125).
- Developer workflow documentation ([docs/developer-workflow.md](../docs/developer-workflow.md)).
- Support for HTTP in Keycloak `master` realm for local development (#130).

### Changed

- Updated `api-gateway` rate limiting to use Redis with dynamic configuration (#128).
- Refactored `share-library` to include shared DTOs and exceptions (#126).

### Fixed

- NullPointerException in `OrderEventProcessor` when handling invalid events (#124).
- Eureka registration timeout for `product-service` under high load (#129).

---

## [0.1.0] - 2025-06-01

### Added

- Initial microservices: `config-server`, `eureka-server`, `api-gateway`, `user-service`, `product-service`,
  `order-service`, `payment-service`, `cart-service`, `shipment-service`, `notification-service`.
- Spring Cloud Config for centralized configuration.
- Eureka for service discovery.
- Keycloak for OAuth2 authentication and authorization.
- Kafka for event-driven communication with CloudEvents.
- Docker Compose setup for local development.
- Monitoring with Zipkin, Prometheus, Grafana, and AKHQ.
- CI/CD pipeline with GitHub Actions.

### Changed

- N/A

### Fixed

- N/A

---

### Key Features of CHANGELOG.md

- **Purpose**: Tracks versioned changes to provide transparency and history.
- **Structure**: Follows the [Keep a Changelog](https://keepachangelog.com/) format with `Added`, `Changed`, and `Fixed`
  sections.
- **Versioning**: Uses Semantic Versioning (e.g., `0.2.0`) for clarity.
- **Examples**: Includes sample entries based on your project’s features and hypothetical issues (e.g., #123, #124).
  Update these with actual changes as your project evolves.
- **Unreleased Section**: Lists planned features for transparency.

---

### Implementation Notes

1. **Create Files**:
    - Save `CONTRIBUTING.md` in the root directory (`ecommerce/CONTRIBUTING.md`).
    - Save `CHANGELOG.md` in the `docs/` directory (`ecommerce/docs/CHANGELOG.md`).
2. **Update global.md**: Ensure the revised `global.md` (from your previous request) links to these files:
    - `CONTRIBUTING.md` in the [Contributing](/README.md#contributing) section.
3. **Customize CHANGELOG.md**:
    - Replace sample entries (e.g., #123, #124) with actual changes from your project’s commit history or GitHub issues.
    - Update the `Unreleased` section with your planned features (e.g.,
      from [Future Enhancements](/README.md#future-enhancements)).
4. **Add GitHub Templates**:
    - Create `.github/ISSUE_TEMPLATE/bug_report.md` and `.github/ISSUE_TEMPLATE/feature_request.md` to standardize issue
      reporting, as referenced in `CONTRIBUTING.md`.
    - Example for `bug_report.md`:
      ```markdown
      ---
      name: Bug Report
      about: Report a bug to help us improve
      ---
      **Describe the bug**
      A clear description of the bug.
 
      **To Reproduce**
      Steps to reproduce the behavior:
      1. Go to '...'
      2. Click on '...'
      3. See error
 
      **Expected behavior**
      A description of what you expected.
 
      **Environment**
      - Java version: [e.g., 21]
      - Docker version: [e.g., 24.0.6]
      - Service: [e.g., order-service]
 
      **Logs/Screenshots**
      Attach relevant logs or screenshots.
      ```

5. **Link in CI/CD**: Update `.github/workflows/build.yml` to check for `CHANGELOG.md` updates in pull requests if
   desired.

---

### Additional Suggestions

- **GitHub Actions for Validation**: Add a GitHub Action to ensure `CHANGELOG.md` is updated for every PR affecting
  code.
- **Visual Aids**: Include a contribution flowchart in `CONTRIBUTING.md` using Mermaid (supported by GitHub Markdown):
  ```mermaid
  graph TD
      A[Start] --> B[Fork Repository]
      B --> C[Create Feature Branch]
      C --> D[Make Changes]
      D --> E[Run Tests]
      E --> F[Submit Pull Request]
      F --> G[Address Feedback]
      G --> H[Merged]
  ```

- **Email Placeholder**: Replace `[your-email@example.com]` in `CONTRIBUTING.md` with your actual contact email or a
  project-specific email.
- **Versioning Strategy**: Formalize your versioning strategy (e.g., Semantic Versioning) in `CHANGELOG.md` and
  communicate it in `CONTRIBUTING.md`.