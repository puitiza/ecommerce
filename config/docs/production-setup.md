# Production Setup

This document outlines the steps to prepare the e-commerce microservices for production deployment on **Azure** using **Kubernetes**, **Azure Key Vault**, and **Azure Application Insights**.

## Optimizations for Production

### Disable Zipkin in Production
Use a `prod` profile to disable Zipkin and enable Azure Application Insights for tracing.

#### Root `build.gradle`

```groovy
ext {
    profile = project.hasProperty('profile') ? project.property('profile') : 'dev'
    enableZipkin = profile == 'dev'
}

def applyTracingDependencies = { dependencyHandler ->
    if (enableZipkin == 'true') {
        dependencyHandler.implementation 'io.micrometer:micrometer-tracing-bridge-brave'
        dependencyHandler.implementation 'io.zipkin.reporter2:zipkin-reporter-brave'
    }
}
```

#### `application-prod.yml` (per service)
```yaml
management:
  tracing:
    enabled: false
  azure:
    application-insights:
      connection-string: ${APPLICATION_INSIGHTS_CONNECTION_STRING}
```

### Remove Redis in Production
For services using Redis (e.g., `api-gateway` for rate limiting, `cart-service`), configure Azure API Gateway to handle rate limiting and remove Redis from the production `docker-compose.yml` or Kubernetes manifests.

### Kubernetes Deployment
Deploy services to Azure Kubernetes Service (AKS) with manifests. Example for `config-server`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: config-server
  namespace: ecommerce
spec:
  replicas: 1
  selector:
    matchLabels:
      app: config-server
  template:
    metadata:
      labels:
        app: config-server
    spec:
      containers:
      - name: config-server
        image: your-registry/ecommerce-config-server:0.0.1-SNAPSHOT
        ports:
        - containerPort: 8885
        env:
        - name: SPRING_PROFILE
          value: prod
        - name: GITHUB_TOKEN
          valueFrom:
            secretKeyRef:
              name: config-server-secrets
              key: github-token
---
apiVersion: v1
kind: Service
metadata:
  name: config-server
  namespace: ecommerce
spec:
  selector:
    app: config-server
  ports:
  - port: 8885
    targetPort: 8885
  type: ClusterIP
```

### Azure Key Vault
- Store sensitive data (e.g., Keycloak credentials, API keys) in Azure Key Vault.
- Configure services to retrieve secrets using Azure SDK or Kubernetes secrets.

### Monitoring
- Use **Azure Application Insights** for distributed tracing and metrics.
- Use **Azure Monitor** for logs and performance monitoring.

## Steps for Production Deployment
1. Build Docker images and push to a registry (e.g., Azure Container Registry).
2. Create Kubernetes manifests for each service.
3. Configure Azure Key Vault for secrets management.
4. Set up Azure API Gateway for rate limiting and routing.
5. Deploy to AKS using `kubectl apply -f k8s/`.
6. Configure Application Insights and Azure Monitor for monitoring.

## Resources
- [Azure Kubernetes Service Documentation](https://docs.microsoft.com/en-us/azure/aks/)
- [Azure Key Vault](https://docs.microsoft.com/en-us/azure/key-vault/)
- [Azure Application Insights](https://docs.microsoft.com/en-us/azure/azure-monitor/app/app-insights-overview)