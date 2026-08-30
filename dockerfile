# =========================================================================
# STAGE 1: Build and Compile the Jar Package
# =========================================================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy the pom.xml file first to cache project dependency downloads
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy your source code and build the production jar file
COPY src ./src
RUN mvn clean package -DskipTests

# =========================================================================
# STAGE 2: Lightweight Runtime Environment
# =========================================================================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy only the compiled jar artifact from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port 8080 to match your Kubernetes ContainerPort configuration
EXPOSE 8080

# Configure execution entrypoint settings
ENTRYPOINT ["java", "-jar", "app.jar"]
