FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# Copy all files from the backend directory
COPY . /workspace/app

# Build the Spring Boot application, making gradlew executable first
RUN chmod +x gradlew
RUN ./gradlew clean build -x test

# Runtime image
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
COPY --from=build /workspace/app/build/libs/*.jar app.jar

# Expose port 8080 and set the environment variables via Render
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
