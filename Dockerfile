#stage 1: Build
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

#TO compile the service into a jar excecutable file
COPY src ./src
RUN mvn clean package -DskipTests

#Stage 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# needed for the docker-compose healthcheck
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

#Copy the jar
COPY --from=build /app/target/*.jar app.jar

#Expose the port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]