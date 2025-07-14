# /Users/anthonypuitiza/Documents/SpringBootProjects/Microservices/ecommerce/config-server/Dockerfile

# Build stage
FROM gradle:jdk21 AS builder

# Set the working directory to the root of your multi-module project INSIDE the container
WORKDIR /app

# Copy the entire project context (this is the 'ecommerce' root)
# The context is set to '.' in docker-compose.yml, so this copies everything from 'ecommerce' to '/app'
COPY . .

# Ensure Gradle Wrapper is executable and build the specific module
# Now, ./gradlew is found at /app (WORKDIR)
# :config-server:build tells Gradle to build only the config-server module
RUN chmod +x ./gradlew && ./gradlew :config-server:build -x test

# Runtime stage
FROM openjdk:21-jdk-slim
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*


# Set the working directory for the final application
WORKDIR /app

# Copy the built JAR from the builder stage
# The JAR will be located at /app/config-server/build/libs/ within the builder stage
COPY --from=builder /app/config-server/build/libs/config-server-0.0.1-SNAPSHOT.jar app.jar

# Expose the port that the application listens on # Make sure this matches the actual port your config-server runs on
EXPOSE 8888

# Run the application
CMD ["java", "-jar", "app.jar"]