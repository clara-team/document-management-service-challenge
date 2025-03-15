FROM maven:3.8.3-openjdk-17-slim as builder

WORKDIR /build/

COPY pom.xml /build/

# Download all required dependencies into one layer
RUN mvn -B -f pom.xml dependency:go-offline

COPY src /build/src/
# Build application
RUN mvn -B -Dmaven.test.skip=true install

FROM openjdk:17.0-jdk-slim

ENV LANG en_GB.UTF-8

WORKDIR /opt/app

COPY --from=builder /build/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]
