# Use Maven image to build the project
FROM maven:3.8.1-openjdk-17-slim AS build

# Create a directory for the project
RUN mkdir /project

# Copy the current directory contents into the container at /project
COPY . /project

# Set the working directory to /project
WORKDIR /project

# Build and install the OpenAPI generated sources
RUN cd src/main/gen && mvn install

# Package the application
RUN mvn -Pprod clean package -DskipTests

# Use OpenJDK image to run the application
FROM openjdk:17-jdk-slim

# Label the image
LABEL authors="dfurchert"

# Create a directory for the application
RUN mkdir /app

# Create a group and user for running the application
RUN groupadd -g 1001 tecogroup && \
    useradd -u 1001 -g tecogroup -s /bin/sh -d /app teco

# Copy the built war from the build stage to the /app directory
COPY --from=build /project/target/*.war /app/iotapp.jar

# Set the working directory to /app
WORKDIR /app

# Change the ownership of the /app directory to the teeco user
RUN chown -R teco:tecogroup /app

# Specify the user to run the app
USER teco

# Command to run the application
CMD java $JAVA_OPTS -jar iotapp.jar
