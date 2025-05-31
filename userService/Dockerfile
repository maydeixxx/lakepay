FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY src src
COPY pom.xml pom.xml
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/target/userService-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]