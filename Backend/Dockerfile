# ==========================
# Stage 1: Build the Spring Boot JAR using Maven
# ==========================
FROM maven:3.9.9-eclipse-temurin-17 AS builder

# Set working directory inside the container
WORKDIR /app

# Copy the Maven project files
COPY pom.xml .
COPY src ./src

# Build the Spring Boot application (skip tests for faster build)
RUN mvn clean package -DskipTests


# ==========================
# Stage 2: Run the built application
# ==========================
FROM eclipse-temurin:17-jdk

# Set working directory for the runtime
WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the default Spring Boot port (Render uses dynamic PORT)
EXPOSE 8080

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]

