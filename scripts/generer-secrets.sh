#!/bin/bash
# Genere .env.prod pour un deploiement : domaine, e-mail Let's Encrypt et NOUVEAUX secrets aleatoires (192 bits chacun).
# Usage : scripts/generer-secrets.sh <e-mail-letsencrypt> [domaine]      (domaine par defaut : logintegrite.asce-lc.bf)
# Ne remplace jamais un .env.prod existant. Les secrets ne sont pas affiches : conservez .env.prod dans un
# gestionnaire de mots de passe ou un coffre (le perdre = perdre l'acces a la base et a Keycloak).
set -euo pipefail

cd "$(dirname "$0")/.."
EMAIL="${1:?Usage : $0 <e-mail-letsencrypt> [domaine]}"
DOMAINE="${2:-logintegrite.asce-lc.bf}"

[ -e .env.prod ] && { echo ".env.prod existe deja : je ne l'ecrase pas (renommez-le ou supprimez-le d'abord)." >&2; exit 1; }
command -v openssl >/dev/null || { echo "openssl est requis pour generer les secrets." >&2; exit 1; }
[[ "$EMAIL" == *@*.* ]] || { echo "Adresse e-mail invalide : $EMAIL" >&2; exit 1; }

secret() { openssl rand -hex 24; }   # hexadecimal : sans caractere special, sur pour SQL et YAML

umask 077                              # fichier lisible par son proprietaire seulement
: > .env.prod
compteur=0
while IFS= read -r ligne || [ -n "$ligne" ]; do
    case "$ligne" in
        DOMAIN=*)    echo "DOMAIN=$DOMAINE" >> .env.prod ;;
        TLS_MODE=*)  echo "TLS_MODE=$EMAIL" >> .env.prod ;;
        *=change-me) echo "${ligne%change-me}$(secret)" >> .env.prod; compteur=$((compteur + 1)) ;;
        *)           echo "$ligne" >> .env.prod ;;
    esac
done < .env.prod.example

echo ".env.prod cree : domaine $DOMAINE, TLS via Let's Encrypt ($EMAIL), $compteur secrets aleatoires generes."
echo "Verifiez qu'il n'y reste aucun 'change-me' : $(grep -c 'change-me' .env.prod) occurrence(s)."
echo "Etape suivante : docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build   (voir DEPLOIEMENT.md)"
