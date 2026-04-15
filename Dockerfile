# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Copy Maven wrapper and pom files
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY spring-ops-mcp-core/pom.xml spring-ops-mcp-core/
COPY spring-ops-mcp-starter/pom.xml spring-ops-mcp-starter/
COPY spring-ops-mcp-server/pom.xml spring-ops-mcp-server/

# Download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source and build
COPY spring-ops-mcp-core/src spring-ops-mcp-core/src
COPY spring-ops-mcp-starter/src spring-ops-mcp-starter/src
COPY spring-ops-mcp-server/src spring-ops-mcp-server/src

RUN ./mvnw clean package -DskipTests -pl spring-ops-mcp-server -am

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -g 1000 spring && adduser -u 1000 -G spring -D spring
USER spring:spring

# Copy the built JAR
COPY --from=build /workspace/spring-ops-mcp-server/target/*.jar app.jar

# Expose MCP server port
EXPOSE 8090

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s \
  CMD wget -q --spider http://localhost:8090/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
