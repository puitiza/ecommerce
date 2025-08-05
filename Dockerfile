# Build stage
FROM gradle:jdk21 AS builder
WORKDIR /app
ARG MODULE=default

# 1. Copy only the gradle configuration files
COPY build.gradle settings.gradle gradle.properties /app/
COPY gradle /app/gradle
COPY shared-library/build.gradle /app/shared-library/
COPY ${MODULE}/build.gradle /app/${MODULE}/

# 2. Download units to take advantage of the cache
RUN gradle --no-daemon :${MODULE}:dependencies

# 3. Copy the source code
COPY shared-library /app/shared-library
COPY ${MODULE} /app/${MODULE}

# 4. Build the application
RUN gradle --no-daemon :${MODULE}:build -x test

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
ARG MODULE=default
COPY --from=builder /app/${MODULE}/build/libs/${MODULE}-0.0.1-SNAPSHOT.jar app.jar
ARG PORT=8080
EXPOSE ${PORT}
CMD ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]