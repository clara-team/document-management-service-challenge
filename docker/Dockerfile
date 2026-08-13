# Use a lightweight JDK image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the jar file from the build context into the container.
# Make sure you have built the jar file (e.g., via Maven/Gradle) before building the Docker image.
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Expose the port your Spring Boot application runs on (default is 8080)
EXPOSE 8085

# Run the application with the defined JAVA_OPTS to enforce the 50MB memory limit.
# ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
CMD ["java", "-jar", "app.jar"]