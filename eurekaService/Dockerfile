#FROM maven:3.9.6-eclipse-temurin-21 AS builder
#WORKDIR /app
#COPY src src
#COPY pom.xml pom.xml
#RUN mvn clean package -DskipTests

FROM openjdk:21
WORKDIR /app
COPY target/eurekaService-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
