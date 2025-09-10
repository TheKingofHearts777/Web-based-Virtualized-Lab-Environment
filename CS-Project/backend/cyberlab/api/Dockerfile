FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml /app

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean install -U

FROM openjdk:21
WORKDIR /app

COPY --from=build /app/target/Cyberlab-API-0.0.1-SNAPSHOT.jar /app/app.jar

# Internal only all API calls are routed through gateway service
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
