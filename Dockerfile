# Stage 2: Create the runtime image
FROM arm64v8/eclipse-temurin:21-jre-alpine

# Label the image
LABEL authors="dfurchert"

# Copy the built artifact from your local file system
# Replace 'path/to/your/iotapp.jar' with the actual path to the JAR file on your local system
COPY /target/iotapp-1.1.jar /usr/app/iotapp.jar

# Set the working directory for the application
WORKDIR /usr/app

# Expose the port the application runs on
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "iotapp.jar"]
