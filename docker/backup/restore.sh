#!/bin/sh
# Restauration d'une sauvegarde. Par defaut restaure dans une base de VERIFICATION
# (logintegrite_db_restore) sans toucher a la production ; passer CIBLE=logintegrite_db pour
# ecraser la base reelle (arreter le backend avant).
#
# Usage (depuis le dossier du compose de production) :
#   docker compose -f docker-compose.prod.yml exec backup /restore.sh 20260929T120000Z
#   docker compose -f docker-compose.prod.yml exec backup /restore.sh          # derniere sauvegarde
# Necessite un role autorise a creer une base : PGUSER=lip_admin (variable RESTORE_PGUSER / RESTORE_PGPASSWORD).
set -eu

DEST=/backups
CIBLE="${CIBLE:-logintegrite_db_restore}"

if [ $# -ge 1 ]; then
    horodatage="$1"
else
    horodatage=$(ls -1 "$DEST"/logintegrite_db-*.dump | sed 's/.*logintegrite_db-\(.*\)\.dump/\1/' | sort | tail -1)
fi
fichier="$DEST/logintegrite_db-$horodatage.dump"
[ -f "$fichier" ] || { echo "Sauvegarde introuvable : $fichier"; exit 1; }

echo "[restore] verification des empreintes de $horodatage"
(cd "$DEST" && sha256sum -c "SHA256SUMS-$horodatage")

export PGUSER="${RESTORE_PGUSER:?RESTORE_PGUSER requis}"
export PGPASSWORD="${RESTORE_PGPASSWORD:?RESTORE_PGPASSWORD requis}"

echo "[restore] restauration vers la base $CIBLE"
dropdb --if-exists "$CIBLE"
createdb "$CIBLE"
pg_restore --no-owner --no-privileges -d "$CIBLE" "$fichier"
echo "[restore] termine : $(psql -tA -d "$CIBLE" -c 'select count(*) from audit.journal_audit') entrees d'audit restaurees"
