#!/bin/sh
# Production : creation des roles a moindre privilege (execute une seule fois, a la creation du volume).
#   lip_admin (POSTGRES_USER)   super-utilisateur, jamais utilise par les applications
#   logintegrite_owner         proprietaire de la base metier : Flyway (DDL, triggers, migrations)
#   logintegrite_app           execution du backend : DML seulement (droits poses par la migration V5)
#   keycloak                   proprietaire de keycloak_db
#   logintegrite_backup        lecture seule (pg_read_all_data) pour pg_dump
set -eu

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<EOSQL
CREATE ROLE logintegrite_owner  LOGIN PASSWORD '${DB_OWNER_PASSWORD}';
CREATE ROLE logintegrite_app    LOGIN PASSWORD '${DB_APP_PASSWORD}';
CREATE ROLE keycloak            LOGIN PASSWORD '${KC_DB_PASSWORD}';
CREATE ROLE logintegrite_backup LOGIN PASSWORD '${DB_BACKUP_PASSWORD}' IN ROLE pg_read_all_data;

ALTER DATABASE ${POSTGRES_DB} OWNER TO logintegrite_owner;
REVOKE ALL ON DATABASE ${POSTGRES_DB} FROM PUBLIC;
GRANT CONNECT ON DATABASE ${POSTGRES_DB} TO logintegrite_app, logintegrite_backup;

REVOKE ALL ON SCHEMA public FROM PUBLIC;

CREATE DATABASE keycloak_db OWNER keycloak;
REVOKE ALL ON DATABASE keycloak_db FROM PUBLIC;
GRANT CONNECT ON DATABASE keycloak_db TO logintegrite_backup;
EOSQL
