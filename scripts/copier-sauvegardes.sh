#!/bin/bash
# Copie la DERNIERE sauvegarde du service "backup" hors du serveur, chiffree (AES-256).
# Les sauvegardes contiennent des donnees personnelles : elles ne quittent jamais le serveur en clair.
#
# Usage : scripts/copier-sauvegardes.sh
# Variables (fichier .env.prod ou environnement) :
#   SAUVEGARDE_DEST          obligatoire : destination "utilisateur@serveur:/chemin/" (scp) ou dossier local/monte
#   SAUVEGARDE_PASSPHRASE    obligatoire : fichier contenant la phrase secrete de chiffrement (chmod 600)
#   SAUVEGARDE_SSH_OPTIONS   optionnel   : ex. "-i /home/administrator/.ssh/id_sauvegarde -p 22"
#   SAUVEGARDE_CONSERVER     optionnel   : nombre de jeux conserves sur la destination (defaut 14)
#   SAUVEGARDE_SOURCE        optionnel   : dossier local des sauvegardes (tests) ; sinon lu dans le conteneur lip-backup
#
# Restauration : voir DEPLOIEMENT.md (dechiffrement avec openssl, puis restore.sh).
# Conservez la phrase secrete HORS du serveur : sans elle, les copies sont inutilisables.
set -euo pipefail

cd "$(dirname "$0")/.."
[ -f .env.prod ] && { set -a; . ./.env.prod 2>/dev/null || true; set +a; }

DEST="${SAUVEGARDE_DEST:?SAUVEGARDE_DEST manquant (ex. administrator@lp:/srv/sauvegardes/logintegrite/)}"
PASSPHRASE="${SAUVEGARDE_PASSPHRASE:?SAUVEGARDE_PASSPHRASE manquant (fichier contenant la phrase secrete)}"
CONSERVER="${SAUVEGARDE_CONSERVER:-14}"
SSH_OPTIONS="${SAUVEGARDE_SSH_OPTIONS:-}"

command -v openssl >/dev/null || { echo "openssl requis" >&2; exit 1; }
[ -r "$PASSPHRASE" ] || { echo "Phrase secrete illisible : $PASSPHRASE" >&2; exit 1; }
[ "$(stat -c '%a' "$PASSPHRASE")" = "600" ] || { echo "Le fichier de phrase secrete doit etre en chmod 600." >&2; exit 1; }

umask 077
TRAVAIL="$(mktemp -d)"
trap 'rm -rf "$TRAVAIL"' EXIT

# 1. Recuperer les sauvegardes
if [ -n "${SAUVEGARDE_SOURCE:-}" ]; then
    SOURCE="$SAUVEGARDE_SOURCE"
else
    docker cp lip-backup:/backups/. "$TRAVAIL/source" >/dev/null
    SOURCE="$TRAVAIL/source"
fi

# 2. Dernier jeu complet (fichier d'empreintes le plus recent)
DERNIER="$(ls -1 "$SOURCE"/SHA256SUMS-* 2>/dev/null | sort | tail -n 1 || true)"
[ -n "$DERNIER" ] || { echo "Aucune sauvegarde avec empreintes dans $SOURCE" >&2; exit 1; }
HORODATAGE="${DERNIER##*SHA256SUMS-}"
echo "Jeu de sauvegarde : $HORODATAGE"

# 3. Verifier les empreintes AVANT de chiffrer (une copie alteree ne doit jamais partir en exil)
( cd "$SOURCE" && sha256sum -c "SHA256SUMS-$HORODATAGE" ) >/dev/null \
    || { echo "ERREUR : empreintes invalides, copie annulee." >&2; exit 1; }

# 4. Chiffrer
mkdir -p "$TRAVAIL/sortie"
for f in $(awk '{print $2}' "$SOURCE/SHA256SUMS-$HORODATAGE") "SHA256SUMS-$HORODATAGE"; do
    openssl enc -aes-256-cbc -pbkdf2 -iter 200000 -salt -pass "file:$PASSPHRASE" \
        -in "$SOURCE/$f" -out "$TRAVAIL/sortie/$f.enc"
done
( cd "$TRAVAIL/sortie" && sha256sum ./*.enc > "ENVOI-$HORODATAGE.sha256" )

# 5. Copier vers la destination
if [[ "$DEST" == *:* ]]; then
    # shellcheck disable=SC2086
    scp -q -o BatchMode=yes $SSH_OPTIONS "$TRAVAIL"/sortie/* "$DEST"
else
    mkdir -p "$DEST"
    cp "$TRAVAIL"/sortie/* "$DEST"/
fi

# 6. Purger les jeux anciens sur la destination (local ou distant)
purge() { ls -1 "$1" 2>/dev/null | sed -n 's/^ENVOI-\(.*\)\.sha256$/\1/p' | sort | head -n -"$CONSERVER"; }
if [[ "$DEST" == *:* ]]; then
    HOTE="${DEST%%:*}"; CHEMIN="${DEST#*:}"
    # shellcheck disable=SC2086
    ANCIENS="$(ssh -o BatchMode=yes $SSH_OPTIONS "$HOTE" "ls -1 '$CHEMIN' 2>/dev/null" | sed -n 's/^ENVOI-\(.*\)\.sha256$/\1/p' | sort | head -n -"$CONSERVER" || true)"
    for h in $ANCIENS; do
        # shellcheck disable=SC2086
        ssh -o BatchMode=yes $SSH_OPTIONS "$HOTE" "cd '$CHEMIN' && rm -f *-$h.dump.enc *-$h.tar.gz.enc SHA256SUMS-$h.enc ENVOI-$h.sha256"
    done
else
    for h in $(purge "$DEST"); do
        ( cd "$DEST" && rm -f ./*-"$h".dump.enc ./*-"$h".tar.gz.enc "SHA256SUMS-$h.enc" "ENVOI-$h.sha256" )
    done
fi

echo "OK : jeu $HORODATAGE chiffre et copie vers $DEST ($(ls -1 "$TRAVAIL/sortie" | wc -l) fichiers)."
