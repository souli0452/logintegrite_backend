#!/bin/sh
# Sauvegarde periodique : base metier, base Keycloak et documents deposes.
# Variables : PGHOST, PGUSER, PGPASSWORD, BACKUP_INTERVAL_SECONDS (defaut 86400), BACKUP_KEEP_DAYS (defaut 14)
# Restauration : voir restore.sh
set -eu

DEST=/backups
INTERVAL="${BACKUP_INTERVAL_SECONDS:-86400}"
KEEP="${BACKUP_KEEP_DAYS:-14}"

sauvegarder() {
    horodatage=$(date -u +%Y%m%dT%H%M%SZ)
    echo "[backup] $horodatage debut"
    pg_dump -Fc -d logintegrite_db -f "$DEST/logintegrite_db-$horodatage.dump"
    pg_dump -Fc -d keycloak_db     -f "$DEST/keycloak_db-$horodatage.dump"
    tar czf "$DEST/documents-$horodatage.tar.gz" -C /data storage
    # Empreintes : permettent de detecter une sauvegarde alteree ou tronquee.
    (cd "$DEST" && sha256sum "logintegrite_db-$horodatage.dump" "keycloak_db-$horodatage.dump" "documents-$horodatage.tar.gz" \
        > "SHA256SUMS-$horodatage")
    find "$DEST" -type f -mtime +"$KEEP" -delete
    echo "[backup] $horodatage termine"
}

while true; do
    sauvegarder || echo "[backup] ECHEC de la sauvegarde"
    sleep "$INTERVAL"
done
