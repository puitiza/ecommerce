# Use an official Gradle image with JDK 17
FROM gradle:jdk17

# Set the working directory to /app
WORKDIR /app

# Copy the project files
COPY . .

# Clean the previous build
RUN gradle clean

# Build the application and skip test
RUN gradle build -x test

# Expose the port that the application will run on
EXPOSE 8888

# Specify the command to run on container start
CMD ["java", "-jar", "build/libs/config-server-0.0.1-SNAPSHOT.jar"]