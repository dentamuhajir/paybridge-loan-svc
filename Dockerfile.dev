# Development Dockerfile with live reload support
FROM eclipse-temurin:21-jdk-alpine

# Install Maven + curl
RUN apk add --no-cache maven curl

WORKDIR /app

# Copy pom.xml first and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Don't copy src — we mount it live in docker-compose)
# COPY src ./src
## Download OpenTelemetry Java Agent
#RUN curl -L -o /otel-javaagent.jar \
#  https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar
#
## Attach agent to JVM used by Maven
#ENV JAVA_TOOL_OPTIONS="-javaagent:/otel-javaagent.jar"

# Enable Spring DevTools restart
ENV SPRING_DEVTOOLS_RESTART_ENABLED=true

# Expose the app port
EXPOSE 8084

# Run the app using Maven
CMD ["mvn", "spring-boot:run"]
