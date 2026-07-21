
# Base Image
FROM eclipse-temurin:21-jre

# Metadata
LABEL maintainer="Alok Ranjan Jha"
LABEL application="Document Approval System"

# Working Directory
WORKDIR /app

# Copy Executable JAR
COPY target/Document-Approval-System-*.jar app.jar

# Expose Spring Boot Port
EXPOSE 8080

# Start Application
ENTRYPOINT ["java", "-jar", "app.jar"]