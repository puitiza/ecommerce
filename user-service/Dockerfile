# Build stage
FROM gradle:jdk21 AS builder

# Set the working directory
WORKDIR /app

# Copy the entire project from the root context (ecommerce)
COPY . .

# Ensure Gradle Wrapper is executable and build the user-service module
RUN chmod +x ./gradlew && ./gradlew :user-service:build -x test

# Runtime stage
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/user-service/build/libs/user-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]