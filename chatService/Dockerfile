# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml parent-pom.xml
COPY chatService/pom.xml pom.xml
COPY chatService/src src
RUN mvn install -N -f parent-pom.xml -DskipTests
RUN mvn clean package -f pom.xml -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk as production
WORKDIR /app
COPY --from=build /app/target/chatService-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]

# Development stage
FROM maven:3.9.6-eclipse-temurin-21 AS development
WORKDIR /app
COPY pom.xml pom.xml
COPY chatService/pom.xml chatService/pom.xml
COPY chatService/src chatService/src
EXPOSE 8990
CMD ["mvn", "compile", "spring-boot:run", "-f", "chatService/pom.xml"]