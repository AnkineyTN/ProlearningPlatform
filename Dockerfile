# Stage 1: Build the Spring Boot application 
FROM openjdk:21-jdk-slim AS builder
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN ./mvnw clean package -DskipTests

# -----------------------------------------------------------------------

# Stage 2: Create the final production image 
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring
WORKDIR /app

ARG JAR_FILE=target/*.jar
COPY --from=builder /app/${JAR_FILE} app.jar

EXPOSE 8080

# Command to run the Spring Boot application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
