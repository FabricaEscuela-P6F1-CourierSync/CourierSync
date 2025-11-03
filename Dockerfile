# Multi-stage build for Spring Boot (Maven + JRE 17)
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# Build
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre

ENV JAVA_OPTS=""
WORKDIR /app

# Copy fat jar (assumes Spring Boot repackage)
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080

# Allow overriding Spring config via envs (e.g., SPRING_DATASOURCE_URL)
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]


