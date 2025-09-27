# Use the official OpenJDK 21 image as the base image
FROM openjdk:21-jdk-slim

# Install netcat for database connectivity check
RUN apt-get update && apt-get install -y netcat-openbsd && rm -rf /var/lib/apt/lists/*

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

# Create a startup script to wait for database
RUN echo '#!/bin/bash\n\
echo "Starting MediBook API..."\n\
echo "Database Host: $DB_HOST"\n\
echo "Database Port: $DB_PORT"\n\
echo "Database Name: $DB_NAME"\n\
echo "Database User: $DB_USERNAME"\n\
\n\
# Give database a moment to be ready (Render databases can take time to initialize)\n\
sleep 10\n\
\n\
# Start the application\n\
exec java -Dserver.port=$PORT -jar target/medi-book-api-0.0.1-SNAPSHOT.jar' > /app/start.sh

RUN chmod +x /app/start.sh

# Expose the port that the application will run on
EXPOSE 8080

# Run the startup script
CMD ["/app/start.sh"]