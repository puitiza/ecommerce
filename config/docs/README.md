# Documentation

This directory contains detailed documentation for the e-commerce microservices platform, built with **Spring Boot 3.0**, **Docker**, **Kubernetes**, and other modern technologies. These documents provide in-depth guidance for developers,
contributors, and deployers working on or extending the platform.

---

## Table of Contents

1. [Overview](#overview)
2. [Documentation Files](#documentation-files)
    - [Changelog](#changelog)
    - [Developer Workflow](#developer-workflow)
    - [Multi-Module Setup](#multi-module-setup)
    - [Order State Machine](#order-state-machine)
    - [Production Setup](#production-setup)
3. [How to Use This Documentation](#how-to-use-this-documentation)
4. [Contributing to Documentation](#contributing-to-documentation)
5. [Resources](#resources)

---

## Overview

The `docs/` directory organizes supplementary documentation to support development, deployment, and maintenance of the
e-commerce platform. Each file focuses on a specific aspect, from developer workflows to production deployment. For a
high-level introduction, start with the [main README](/README.md) in the root directory.

---

## Documentation Files

### Changelog

- **File**: [CHANGELOG.md](CHANGELOG.md)
- **Description**: Tracks version history, including new features, bug fixes, and breaking changes. Follows
  the [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) format
  and [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
- **Use Case**: Check for recent updates or prepare for a new release.

### Developer Workflow

- **File**: [developer-workflow.md](developer-workflow.md)
- **Description**: Guides developers through advanced workflows for local development, debugging, and testing, including
  Docker Compose commands and log analysis.
- **Use Case**: Use for setting up a local environment or troubleshooting service issues.

### Multi-Module Setup

- **File**: [multi-module.md](multi-module.md)
- **Description**: Details the multi-module Gradle project structure, including the `share-library` module for shared
  DTOs and utilities.
- **Use Case**: Understand or extend the project’s modular architecture.

### Order State Machine

- **File**: [order-state-machine.md](order-state-machine.md)
- **Description**: Explains the order lifecycle and state transitions in the `order-service` using Spring State Machine.
- **Use Case**: Reference for modifying order processing logic.

### Production Setup

- **File**: [production-setup.md](production-setup.md)
- **Description**: Provides instructions for deploying to Kubernetes, including Azure AKS integration and production
  considerations.
- **Use Case**: Follow for production deployment.

---

## How to Use This Documentation

- **New Developers**: Start with the [main README](/README.md) for setup, then refer
  to [developer-workflow.md](developer-workflow.md) for local development.
- **Contributors**: See [CONTRIBUTING.md](/CONTRIBUTING.md) for guidelines. Update [CHANGELOG.md](CHANGELOG.md) for
  significant changes.
- **Deployers**: Use [production-setup.md](production-setup.md) for Kubernetes deployment.
- **Troubleshooting**: Check troubleshooting sections in each document or
  the [main README](/README.md#troubleshooting).

---

## Contributing to Documentation

We welcome documentation improvements! To contribute:

1. Follow [CONTRIBUTING.md](/CONTRIBUTING.md#submitting-code-changes) for forking and branching.
2. Update existing files or propose new ones (e.g., `testing-strategies.md`).
3. Use Markdown with clear headings, code blocks, and callouts (`> **Note**`).
4. Update this `README.md` for new files.
5. Submit a pull request with a clear description.

Example changelog entry:

```markdown
## [0.3.0] - 2025-09-01

### Added

- Documentation index in `docs/README.md` (#<issue-number>).
```

---

## Resources

- **Main README**: [README.md](../docs/README.md) for project overview and setup.
- **Contributing Guidelines**: [CONTRIBUTING.md](/CONTRIBUTING.md) for contribution instructions.
- **GitHub Issues**: [Issue Tracker](https://github.com/puitiza/ecommerce/issues) for bugs and features.
- **Keep a Changelog**: [Format Guide](https://keepachangelog.com/en/1.0.0/) for `CHANGELOG.md`.
- **Markdown Guide**: [GitHub Markdown](https://docs.github.com/en/get-started/writing-on-github) for formatting.

---
