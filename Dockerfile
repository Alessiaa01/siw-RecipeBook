# FASE 1: Build
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
# Compila tutto
RUN mvn clean package -DskipTests

# FASE 2: Run
FROM openjdk:17-jdk-slim
COPY --from=build /app/target/siw-RecipeBook-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]