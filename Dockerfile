
# Stage 1 - Build the Spring Boot Application
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Maven wrapper
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

# Make wrapper executable
RUN chmod +x mvnw

# Download all dependencies first
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests

# Stage 2 - Runtime Image
FROM eclipse-temurin:21-jre

LABEL maintainer="Alok Ranjan Jha"
LABEL application="Document Approval System"

WORKDIR /app

COPY --from=builder \
/app/target/Document-Approval-System-*.jar \
app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]