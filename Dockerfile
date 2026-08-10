# On utilise une image contenant Maven et Java 17 (modifie le 17 si tu utilises Java 21)
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# On copie le fichier de configuration Maven et le code source
COPY pom.xml .
COPY src ./src

# On compile le projet et on génère le fichier .jar en ignorant les tests pour aller plus vite
RUN mvn clean package -DskipTests

# Étape 2 : Exécution (Run)
# On utilise une image plus légère avec uniquement le JRE (Java Runtime Environment)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# On récupère le fichier .jar généré à l'étape précédente
COPY --from=builder /build/target/*.jar app.jar

# On expose le port sur lequel l'application va tourner
EXPOSE 8080

# Commande de démarrage de l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
