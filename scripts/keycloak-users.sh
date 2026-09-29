#!/bin/bash
# Cree ou supprime des comptes dans le realm logintegrite via l'API d'administration Keycloak.
# Usage :
#   KC_URL=https://exemple/auth KC_ADMIN_PASSWORD=... scripts/keycloak-users.sh create <username> <ROLE> <motdepasse> [email]
#   KC_URL=... KC_ADMIN_PASSWORD=... scripts/keycloak-users.sh delete <username>
# Variables : KC_URL (obligatoire), KC_ADMIN_USER (defaut admin), KC_ADMIN_PASSWORD (obligatoire),
#             CURL_OPTS (ex. "-k --resolve auth.x:443:127.0.0.1" pour un certificat local)
# Le premier ADMIN d'un deploiement se cree ainsi (le realm de production n'importe aucun utilisateur).
set -euo pipefail

: "${KC_URL:?KC_URL requis}"
: "${KC_ADMIN_PASSWORD:?KC_ADMIN_PASSWORD requis}"
KC_ADMIN_USER="${KC_ADMIN_USER:-admin}"
# shellcheck disable=SC2086
CURL="curl -s ${CURL_OPTS:-}"
REALM=logintegrite

jeton_admin() {
    $CURL -X POST "$KC_URL/realms/master/protocol/openid-connect/token" \
        -d grant_type=password -d client_id=admin-cli \
        --data-urlencode "username=$KC_ADMIN_USER" --data-urlencode "password=$KC_ADMIN_PASSWORD" \
        | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p'
}

TOKEN=$(jeton_admin)
[ -n "$TOKEN" ] || { echo "Authentification admin Keycloak impossible" >&2; exit 1; }
AUTH="Authorization: Bearer $TOKEN"

id_utilisateur() {
    $CURL -H "$AUTH" "$KC_URL/admin/realms/$REALM/users?username=$1&exact=true" | sed -n 's/.*"id":"\([^"]*\)".*/\1/p' | head -1
}

case "${1:-}" in
create)
    user="${2:?username}"; role="${3:?ROLE}"; mdp="${4:?motdepasse}"; email="${5:-$user@asce-lc.bf}"
    code=$($CURL -o /dev/null -w '%{http_code}' -X POST -H "$AUTH" -H 'Content-Type: application/json' \
        "$KC_URL/admin/realms/$REALM/users" \
        -d "{\"username\":\"$user\",\"email\":\"$email\",\"enabled\":true,\"emailVerified\":true,\"firstName\":\"$user\",\"lastName\":\"Compte\",\"credentials\":[{\"type\":\"password\",\"value\":\"$mdp\",\"temporary\":false}]}")
    [ "$code" = "201" ] || { echo "Creation de $user : HTTP $code" >&2; exit 1; }
    uid=$(id_utilisateur "$user")
    role_json=$($CURL -H "$AUTH" "$KC_URL/admin/realms/$REALM/roles/$role")
    $CURL -o /dev/null -X POST -H "$AUTH" -H 'Content-Type: application/json' \
        "$KC_URL/admin/realms/$REALM/users/$uid/role-mappings/realm" -d "[$role_json]"
    echo "$user cree avec le role $role"
    ;;
delete)
    user="${2:?username}"; uid=$(id_utilisateur "$user")
    [ -n "$uid" ] || { echo "$user introuvable"; exit 0; }
    $CURL -o /dev/null -X DELETE -H "$AUTH" "$KC_URL/admin/realms/$REALM/users/$uid"
    echo "$user supprime"
    ;;
*)
    echo "Usage : $0 create <username> <ROLE> <motdepasse> [email] | delete <username>" >&2; exit 2 ;;
esac
