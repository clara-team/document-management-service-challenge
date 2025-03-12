FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

# Copy Maven wrapper, pom.xml and Maven settings
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp

# Install curl for health checks
RUN apk --no-cache add curl

# Create a user with reduced privileges
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Create temporary storage directory for file uploads
RUN mkdir -p /tmp/document-uploads
ENV TMPDIR=/tmp/document-uploads

# Extract layers from the build stage
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Add a health check
HEALTHCHECK --interval=30s --timeout=3s --retries=3 CMD curl -f http://localhost:8080/documents-manager/actuator/health || exit 1

# Set the entry point with the correct main class
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp app:app/lib/* com.clara.ops.challenge.document_management_service_challenge.DocumentManagementServiceChallengeApplication"]