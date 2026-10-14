
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

git gi
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/career-compass-api-1.0.0.jar app.jar

EXPOSE 8080

CMD ["sh", "-c", "java -Dserver.port=${PORT:-10000} -jar app.jar"]
