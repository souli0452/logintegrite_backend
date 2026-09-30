#!/bin/bash
# Obtient un jeton d'acces par le flux OIDC reel (code d'autorisation + PKCE) du client public
# logintegrite-backend, comme le fait le navigateur. Affiche le jeton sur la sortie standard.
# Usage : AUTH_URL=https://exemple/auth APP_URL=https://app.exemple scripts/token.sh <utilisateur> <motdepasse>
# Variables : AUTH_URL, APP_URL (obligatoires), CURL_OPTS (ex. "-k --resolve auth.x:443:127.0.0.1")
set -euo pipefail

: "${AUTH_URL:?AUTH_URL requis}"; : "${APP_URL:?APP_URL requis}"
USER_NAME="${1:?utilisateur}"; PASSWORD="${2:?motdepasse}"
# shellcheck disable=SC2086
CURL="curl -s ${CURL_OPTS:-}"

# Couple PKCE fixe (exemple de la RFC 7636) : suffisant pour un test automatise.
VERIFIER="dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"
CHALLENGE="E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM"
JAR=$(mktemp); trap 'rm -f "$JAR"' EXIT
REDIRECT="${APP_URL%/}/"
ENC_REDIRECT=$(printf '%s' "$REDIRECT" | sed 's|:|%3A|g; s|/|%2F|g')

page=$($CURL -c "$JAR" -b "$JAR" "$AUTH_URL/realms/logintegrite/protocol/openid-connect/auth?client_id=logintegrite-backend&redirect_uri=$ENC_REDIRECT&response_type=code&scope=openid&code_challenge=$CHALLENGE&code_challenge_method=S256&state=s&nonce=n")
action=$(echo "$page" | grep -o 'action="[^"]*"' | head -1 | sed 's/action="//; s/"$//; s/&amp;/\&/g')
[ -n "$action" ] || { echo "Formulaire de connexion introuvable" >&2; exit 1; }

location=$($CURL -o /dev/null -w '%{redirect_url}' -c "$JAR" -b "$JAR" -X POST "$action" \
    --data-urlencode "username=$USER_NAME" --data-urlencode "password=$PASSWORD")
code=$(echo "$location" | sed -n 's/.*[?&]code=\([^&]*\).*/\1/p')
[ -n "$code" ] || { echo "Connexion refusee pour $USER_NAME" >&2; exit 1; }

$CURL -X POST "$AUTH_URL/realms/logintegrite/protocol/openid-connect/token" \
    -d grant_type=authorization_code -d client_id=logintegrite-backend \
    --data-urlencode "redirect_uri=$REDIRECT" -d code="$code" -d code_verifier="$VERIFIER" \
    | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p'
