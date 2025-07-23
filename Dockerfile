# Build stage
FROM gradle:jdk21 AS builder

WORKDIR /app

COPY build.gradle settings.gradle gradle.properties /app/
COPY gradle /app/gradle
COPY shared-library /app/shared-library
ARG MODULE=default
COPY ${MODULE} /app/${MODULE}

RUN gradle --no-daemon :${MODULE}:dependencies
RUN gradle --no-daemon :${MODULE}:build -x test

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
ARG MODULE=default
COPY --from=builder /app/${MODULE}/build/libs/${MODULE}-0.0.1-SNAPSHOT.jar app.jar
ARG PORT=8080
EXPOSE ${PORT}
CMD ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]