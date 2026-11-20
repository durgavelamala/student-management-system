# ============================================================
# Dockerfile - Student Management System
#
# This image is built from the JAR that Maven creates.
# Step 1: mvn clean package      -> target/student-management-system.jar
# Step 2: docker build -t student-management-system .
# ============================================================

# Small official image that contains only the Java 17 runtime (JRE)
FROM eclipse-temurin:17-jre

# All following commands run inside /app in the container
WORKDIR /app

# Copy the Maven-built executable JAR into the image
COPY target/student-management-system.jar app.jar

# The Spring Boot application listens on port 8080
EXPOSE 8080

# Command executed when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]
