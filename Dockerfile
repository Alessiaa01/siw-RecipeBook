# FASE 1: Build
# Usiamo Maven con Eclipse Temurin 17 (versione stabile e supportata)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# FASE 2: Run
# Usiamo Eclipse Temurin 17 per far girare l'app (sostituisce la vecchia openjdk)
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/siw-RecipeBook-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]