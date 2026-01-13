# FASE 1: Costruzione (Build)
# Usiamo un'immagine Maven con Java 17 per compilare il progetto
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
# Compiliamo il progetto saltando i test per velocità
RUN mvn clean package -DskipTests

# FASE 2: Esecuzione (Run)
# Usiamo un'immagine leggera di Java 17 per far girare il sito
FROM openjdk:17-jdk-slim
COPY --from=build /app/target/siw-RecipeBook-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]