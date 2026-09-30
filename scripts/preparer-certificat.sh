#!/bin/bash
# Prepare un certificat achete (ex. GoDaddy) pour Caddy : certs/fullchain.pem + certs/privkey.pem.
# Usage : scripts/preparer-certificat.sh <certificat.crt> <chaine-intermediaire.crt> <cle-privee.key> [domaine]
# Controles : la cle correspond au certificat, le domaine est couvert (SAN), le certificat n'est pas expire.
# La cle privee n'est jamais affichee ni versionnee (certs/ est dans .gitignore).
set -euo pipefail
cd "$(dirname "$0")/.."
CRT="${1:?Usage : $0 <certificat.crt> <chaine.crt> <cle.key> [domaine]}"; CHAINE="${2:?chaine manquante}"; CLE="${3:?cle manquante}"
DOMAINE="${4:-logintegrite.asce-lc.bf}"
for f in "$CRT" "$CHAINE" "$CLE"; do [ -r "$f" ] || { echo "Fichier illisible : $f" >&2; exit 1; }; done

# La cle correspond-elle au certificat ? (comparaison des cles publiques)
pub_crt=$(openssl x509 -in "$CRT" -noout -pubkey | openssl sha256)
pub_key=$(openssl pkey -in "$CLE" -pubout | openssl sha256)
[ "$pub_crt" = "$pub_key" ] || { echo "ERREUR : la cle privee ne correspond pas a ce certificat." >&2; exit 1; }

# Le domaine est-il couvert ? (openssl gere les jokers *.asce-lc.bf)
openssl x509 -in "$CRT" -noout -checkhost "$DOMAINE" | grep -q "does match" \
  || { echo "ERREUR : le certificat ne couvre pas $DOMAINE (un certificat pour asce-lc.bf seul ne couvre PAS les sous-domaines ; il faut *.asce-lc.bf ou $DOMAINE)." >&2; exit 1; }

# Expiration : refuse un certificat expire, avertit a moins de 30 jours.
openssl x509 -in "$CRT" -noout -checkend 0 >/dev/null || { echo "ERREUR : certificat expire." >&2; exit 1; }
openssl x509 -in "$CRT" -noout -checkend $((30*86400)) >/dev/null || echo "ATTENTION : le certificat expire dans moins de 30 jours."

umask 077
mkdir -p certs
cat "$CRT" "$CHAINE" > certs/fullchain.pem
cp "$CLE" certs/privkey.pem
chmod 600 certs/privkey.pem; chmod 644 certs/fullchain.pem
echo "OK : certs/fullchain.pem et certs/privkey.pem prets ($(openssl x509 -in "$CRT" -noout -enddate))."
echo "Dans .env.prod : TLS_MODE=/certs/fullchain.pem /certs/privkey.pem"
