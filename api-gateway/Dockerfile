# Use an official Gradle image with JDK 21
FROM gradle:jdk21
# Set the working directory to /app
WORKDIR /app
# Copy the project files
COPY . .
# Clean the previous build
RUN gradle clean
# Build the application
RUN gradle build -x test
# Expose the port that the application will run on
EXPOSE 8080
# Specify the command to run on container start
CMD ["java", "-jar", "build/libs/api-gateway-0.0.1-SNAPSHOT.jar"]