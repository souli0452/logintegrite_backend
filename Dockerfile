# ---- Build : compile et empaquete le jar (Java 17) ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# Les dependances sont resolues dans une couche separee : elle n'est reconstruite que si le pom change.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- Run : JRE seul, utilisateur sans privileges ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app \
    && mkdir -p /app/storage/documents && chown -R app:app /app
COPY --from=builder --chown=app:app /build/target/*.jar app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
