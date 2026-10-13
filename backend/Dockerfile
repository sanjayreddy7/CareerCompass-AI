
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/career-compass-api-1.0.0.jar app.jar
EXPOSE 8080
CMD ["sh", "-c", "java -Dserver.port=${PORT:-10000} -jar app.jar"]
