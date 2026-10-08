#!/bin/bash
# Configure l'envoi de courriels du realm logintegrite (mot de passe oublie) et affiche l'etat.
# A lancer sur le serveur, depuis n'importe quel dossier :
#   scripts/keycloak-smtp.sh appliquer   # applique SMTP_* de .env.prod au realm et active « mot de passe oublie »
#   scripts/keycloak-smtp.sh afficher    # montre la configuration actuelle (le mot de passe est masque)
#   scripts/keycloak-smtp.sh desactiver  # retire le lien « mot de passe oublie » (la messagerie reste configuree)
# Variables lues dans .env.prod (transmises au conteneur par docker-compose.prod.yml) :
#   SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_FROM, SMTP_PASSWORD (obligatoire pour « appliquer »).
# Le mot de passe SMTP ne passe jamais par la ligne de commande : il est lu DANS le conteneur.
set -euo pipefail

cd "$(dirname "$0")/.."
DC=(docker compose -f docker-compose.prod.yml --env-file .env.prod)
KCADM=/opt/keycloak/bin/kcadm.sh

connexion='$KCADM config credentials --server http://localhost:8080/auth --realm master --user "$KC_BOOTSTRAP_ADMIN_USERNAME" --password "$KC_BOOTSTRAP_ADMIN_PASSWORD" >/dev/null'

case "${1:-}" in
appliquer)
    "${DC[@]}" exec -T -e KCADM="$KCADM" keycloak sh -c "
        : \"\${SMTP_PASSWORD:?SMTP_PASSWORD absent de .env.prod}\"
        $connexion &&
        \$KCADM update realms/logintegrite \
            -s \"smtpServer.host=\$SMTP_HOST\" \
            -s \"smtpServer.port=\$SMTP_PORT\" \
            -s \"smtpServer.from=\$SMTP_FROM\" \
            -s 'smtpServer.fromDisplayName=Log Intégrité — ASCE-LC' \
            -s 'smtpServer.starttls=true' \
            -s 'smtpServer.ssl=false' \
            -s 'smtpServer.auth=true' \
            -s \"smtpServer.user=\$SMTP_USER\" \
            -s \"smtpServer.password=\$SMTP_PASSWORD\" \
            -s resetPasswordAllowed=true \
            -s actionTokenGeneratedByUserLifespan=900
    "
    echo "Messagerie appliquee. Testez avec « Mot de passe oublie ? » sur l'ecran de connexion (compte de test, vraie boite)."
    ;;
desactiver)
    "${DC[@]}" exec -T -e KCADM="$KCADM" keycloak sh -c "
        $connexion &&
        \$KCADM update realms/logintegrite -s resetPasswordAllowed=false
    "
    echo "Lien « Mot de passe oublie » retire."
    ;;
afficher)
    "${DC[@]}" exec -T -e KCADM="$KCADM" keycloak sh -c "
        $connexion &&
        \$KCADM get realms/logintegrite --fields resetPasswordAllowed,actionTokenGeneratedByUserLifespan,smtpServer
    "
    ;;
*)
    echo "Usage : $0 appliquer|afficher|desactiver" >&2
    exit 2
    ;;
esac
