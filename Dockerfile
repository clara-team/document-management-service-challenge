# Use an official Maven image as the base image
FROM maven:3.9.9-amazoncorretto-17-alpine AS build
# Set the working directory in the container
WORKDIR /app
# Copy the pom.xml and the project files to the container
COPY pom.xml .
COPY src ./src
# Build the application using Maven
RUN mvn clean package -DskipTests
# Use an official OpenJDK image as the base image
FROM amazoncorretto:17-alpine-jdk AS run
# Set the working directory in the container
WORKDIR /app
# Copy the built JAR file from the previous stage to the container
COPY --from=build /app/target/document-management-service-challenge-0.0.1-SNAPSHOT-LOCAL.jar document-management-service-challenge.jar
# Set port to deploy application
EXPOSE 8080
# Set the command to run the application
#CMD ["java", "-jar", "document-management-service-challenge.jar"]
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "document-management-service-challenge.jar"]
