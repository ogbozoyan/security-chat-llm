# Base image for the build stage
FROM eclipse-temurin:21 AS builder

WORKDIR /app

# Copy Gradle Wrapper and source files
COPY settings.gradle .
COPY build.gradle .
COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY gradle/ gradle/
COPY . core/

# Ensure Gradle wrapper is executable
RUN chmod +x ./gradlew

ENV _JAVA_OPTIONS="-Djavax.net.debug=ssl:handshake"


# Build the project
RUN ./gradlew build --no-daemon

# Runtime stage
FROM eclipse-temurin:21

WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/core/build/libs/*.jar app.jar

# Expose the application port
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]