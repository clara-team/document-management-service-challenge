# Use Maven with OpenJDK 17 as the builder
FROM maven:3.8.3-openjdk-17-slim as builder

# Set the working directory for the build process
WORKDIR /build/

# Copy the Maven POM file to the working directory
COPY pom.xml /build/

# Download all required dependencies into one layer
RUN mvn -B -f pom.xml dependency:go-offline

# Copy the source code to the working directory
COPY src /build/src/

# Apply Spotless formatting
RUN mvn spotless:apply

# Build the application, skipping tests for quicker builds
RUN mvn -B -Dmaven.test.skip=true install

# Use OpenJDK 17 slim image for the final image
FROM openjdk:17.0-jdk-slim

# Set the locale environment variable
ENV LANG en_GB.UTF-8

# Set the working directory for the application
WORKDIR /opt/app

# Copy the built JAR file from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Set the entry point for the container
ENTRYPOINT ["java","-jar","app.jar"]
