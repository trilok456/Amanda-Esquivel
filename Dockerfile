# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the current directory contents into the container at /app
COPY . .

# Build the application (if needed)
# Uncomment the line below if your project uses Gradle to build
# RUN ./gradlew build

# Alternatively, if your project uses Maven, uncomment the line below
# RUN ./mvnw clean package

# Copy the jar file to the image
COPY build/libs/*.jar app.jar

# Expose the port that the application will run on
EXPOSE 8084

# Run the jar file
ENTRYPOINT ["java","-jar","/app/app.jar"]
