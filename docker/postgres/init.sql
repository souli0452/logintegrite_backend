-- Execute uniquement a la creation du volume Postgres.
-- Base dediee a Keycloak (separee de la base metier logintegrite_db).
-- Le schema metier (schemas, types, tables, triggers d'audit, referentiels) est cree et versionne
-- par Flyway au demarrage du backend (src/main/resources/db/migration).
CREATE DATABASE keycloak_db OWNER logintegrite_user;
