# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml parent-pom.xml
COPY gatewayApi/pom.xml pom.xml
COPY gatewayApi/src src
RUN mvn install -N -f parent-pom.xml -DskipTests
RUN mvn clean package -f pom.xml -DskipTests


# Production stage
FROM openjdk:21 as production
WORKDIR /app
COPY --from=build /app/target/gatewayApi-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8990
ENTRYPOINT ["java", "-jar", "app.jar"]

# Development stage
FROM maven:3.9.6-eclipse-temurin-21 AS development
WORKDIR /app
COPY pom.xml pom.xml
COPY gatewayApi/pom.xml gatewayApi/pom.xml
COPY gatewayApi/src gatewayApi/src
EXPOSE 8990
CMD ["mvn", "compile", "spring-boot:run", "-f", "gatewayApi/pom.xml"]