# Build stage
FROM gradle:jdk21 AS builder

# Set working directory
WORKDIR /app

# Copy Gradle configuration files to cache dependencies
COPY build.gradle settings.gradle gradle.properties /app/
COPY gradle /app/gradle

# Copy only the specific module's source code
ARG MODULE
COPY $MODULE $MODULE

# Cache dependencies
RUN gradle --no-daemon :${MODULE}:dependencies

# Build the specific module, skipping tests
RUN gradle --no-daemon :${MODULE}:build -x test

# Runtime stage
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the built JAR
ARG MODULE
COPY --from=builder /app/${MODULE}/build/libs/${MODULE}-0.0.1-SNAPSHOT.jar app.jar

# Expose the port (provided via ARG)
ARG PORT=8080
EXPOSE ${PORT}

# Run the application with optimized JVM settings
CMD ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]