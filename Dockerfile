# Use the official OpenJDK 21 image as the base image
FROM openjdk:21-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper and pom.xml to download dependencies
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Make the Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose the port that the application will run on
EXPOSE 8080

# Run the application using the PORT environment variable provided by Render
CMD ["sh", "-c", "java -Dserver.port=$PORT -jar target/medi-book-api-0.0.1-SNAPSHOT.jar"]