FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY discovery-service/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]