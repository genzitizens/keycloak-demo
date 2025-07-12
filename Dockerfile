# Use Liberica or OpenJDK base image for Java 21 (as you're using Liberica JDK 21)
FROM bellsoft/liberica-runtime-container:jre-21

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8001

# Create directory for the app
WORKDIR /app

# Copy app JAR file (this assumes you build it before Docker build)
COPY target/keycloak-demo.jar app.jar

# Expose the app port (change if you use a different one)
EXPOSE 8001

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
