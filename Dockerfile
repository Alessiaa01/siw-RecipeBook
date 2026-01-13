# FASE 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .

# Modifichiamo questa riga per gestire meglio i problemi di rete
RUN mvn clean package -DskipTests -Dmaven.wagon.http.retryHandler.count=3

# FASE 2: Run
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/siw-RecipeBook-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]