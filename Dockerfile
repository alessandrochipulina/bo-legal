# Dockerfile for a Spring WebFlux application with PostgreSQL and Maven
# This Dockerfile builds a Spring WebFlux application using Maven and runs it in a container with PostgreSQL.
# It uses a multi-stage build to create a smaller final image.
# Stage 1: Build the application

# Use an official OpenJDK 24 runtime as a parent image
FROM eclipse-temurin:24-jdk AS builder

# Set the working directory in the container
WORKDIR /app

# Copy all
COPY  . .

# Install Maven
RUN apt-get update && apt-get install -y maven
# Build the application using Maven
RUN ./mvnw clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:24-jre

# Set the working directory in the container
WORKDIR /app

# Copy the jar file from the builder stage to the runtime image
COPY --from=builder /app/target/*.jar app.jar

# Expose the port your Spring WebFlux application runs on
EXPOSE 8080

# Set environment variables for PostgreSQL
ENV SPRING_R2DBC_URL=r2dbc:postgresql://127.0.0.1:5432/postgres
ENV SPRING_R2DBC_USERNAME=postgres
ENV SPRING_R2DBC_PASSWORD=postgres

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]


