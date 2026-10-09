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

# Execute dans le conteneur Keycloak le programme lu sur l'entree standard.
dans_keycloak() {
    "${DC[@]}" exec -T keycloak sh -s
}

case "${1:-}" in
appliquer)
    dans_keycloak <<'EOS'
K=/opt/keycloak/bin/kcadm.sh
[ -n "$SMTP_PASSWORD" ] || { echo "ERREUR : SMTP_PASSWORD absent de .env.prod" >&2; exit 1; }
$K config credentials --server http://localhost:8080/auth --realm master \
    --user "$KC_BOOTSTRAP_ADMIN_USERNAME" --password "$KC_BOOTSTRAP_ADMIN_PASSWORD" >/dev/null
$K update realms/logintegrite \
    -s "smtpServer.host=$SMTP_HOST" \
    -s "smtpServer.port=$SMTP_PORT" \
    -s "smtpServer.from=$SMTP_FROM" \
    -s 'smtpServer.fromDisplayName=Log Intégrité — ASCE-LC' \
    -s 'smtpServer.starttls=true' \
    -s 'smtpServer.ssl=false' \
    -s 'smtpServer.auth=true' \
    -s "smtpServer.user=$SMTP_USER" \
    -s "smtpServer.password=$SMTP_PASSWORD" \
    -s resetPasswordAllowed=true \
    -s actionTokenGeneratedByUserLifespan=900
# Verification : kcadm ne montre le contenu de smtpServer qu'avec smtpServer(*)
resultat=$($K get realms/logintegrite --fields "smtpServer(*)")
case "$resultat" in
    *'"host"'*) ;;
    *) echo "ERREUR : smtpServer vide apres la mise a jour" >&2; exit 1 ;;
esac
EOS
    echo "Messagerie appliquee. Testez avec « Mot de passe oublie ? » sur l'ecran de connexion (compte de test, vraie boite)."
    ;;
desactiver)
    dans_keycloak <<'EOS'
K=/opt/keycloak/bin/kcadm.sh
$K config credentials --server http://localhost:8080/auth --realm master \
    --user "$KC_BOOTSTRAP_ADMIN_USERNAME" --password "$KC_BOOTSTRAP_ADMIN_PASSWORD" >/dev/null
$K update realms/logintegrite -s resetPasswordAllowed=false
EOS
    echo "Lien « Mot de passe oublie » retire."
    ;;
afficher)
    dans_keycloak <<'EOS'
K=/opt/keycloak/bin/kcadm.sh
$K config credentials --server http://localhost:8080/auth --realm master \
    --user "$KC_BOOTSTRAP_ADMIN_USERNAME" --password "$KC_BOOTSTRAP_ADMIN_PASSWORD" >/dev/null
$K get realms/logintegrite --fields "resetPasswordAllowed,actionTokenGeneratedByUserLifespan,smtpServer(*)"
EOS
    ;;
*)
    echo "Usage : $0 appliquer|afficher|desactiver" >&2
    exit 2
    ;;
esac
