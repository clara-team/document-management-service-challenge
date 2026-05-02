FROM ghcr.io/graalvm/native-image-community:17 AS builder

WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

COPY src ./src
RUN ./mvnw -Pnative native:compile -DskipTests -q

FROM debian:bookworm-slim

WORKDIR /app

COPY --from=builder /app/target/document-management-service-challenge .
COPY --from=builder /app/target/lib*.so* ./

EXPOSE 8080

ENTRYPOINT ["./document-management-service-challenge", "-Xmx48m"]
