#!/bin/bash

# controle qui echoue a la fin de l'execution ; a lancer apres chaque deploiement.
#
# Prerequis : 4 comptes de test (roles ADMIN, AGENT, VALIDATEUR, CONSULTANT), crees avec
#   scripts/keycloak-users.sh, puis supprimes une fois le test termine.
# Variables :
#   API_URL      ex. https://exemple/api/v1                   (obligatoire)
#   AUTH_URL     ex. https://exemple/auth                     (obligatoire)
#   APP_URL      ex. https://exemple                          (obligatoire)
#   SMOKE_PASSWORD   mot de passe commun des 4 comptes         (obligatoire)
#   SMOKE_PREFIX     prefixe des comptes (defaut "smoke")  -> smoke.admin, smoke.agent, ...
#   CURL_OPTS        options curl supplementaires (ex. "-k --resolve api.x:443:127.0.0.1 --resolve auth.x:443:127.0.0.1")
set -uo pipefail

: "${API_URL:?}"; : "${AUTH_URL:?}"; : "${APP_URL:?}"; : "${SMOKE_PASSWORD:?}"
PREFIX="${SMOKE_PREFIX:-smoke}"
HERE="$(cd "$(dirname "$0")" && pwd)"
export AUTH_URL APP_URL CURL_OPTS
# shellcheck disable=SC2086
C="curl -s ${CURL_OPTS:-}"
OK=0; KO=0
TMP=$(mktemp -d); trap 'rm -rf "$TMP"' EXIT
# curl natif Windows : les chemins dans -F @fichier doivent etre au format Windows (cygpath absent sous Linux)
TMPF=$(cygpath -m "$TMP" 2>/dev/null || echo "$TMP")

verifier() { # verifier <description> <attendu> <obtenu>
    if [ "$2" = "$3" ]; then OK=$((OK+1)); printf '  [OK] %s\n' "$1"
    else KO=$((KO+1)); printf '  [ECHEC] %s (attendu %s, obtenu %s)\n' "$1" "$2" "$3"; fi
}
contient() { # contient <description> <fichier> <motif>
    if grep -qi -- "$3" "$2"; then OK=$((OK+1)); printf '  [OK] %s\n' "$1"
    else KO=$((KO+1)); printf '  [ECHEC] %s (motif "%s" absent)\n' "$1" "$3"; fi
}
http() { # http <jeton> <methode> <chemin> [corps json]  -> ecrit $TMP/out, affiche le code HTTP
    local jeton="$1" methode="$2" chemin="$3" corps="${4:-}"
    if [ -n "$corps" ]; then
        $C -o "$TMP/out" -w '%{http_code}' -X "$methode" -H "Authorization: Bearer $jeton" -H 'Content-Type: application/json' -d "$corps" "$API_URL$chemin"
    else
        $C -o "$TMP/out" -w '%{http_code}' -X "$methode" -H "Authorization: Bearer $jeton" "$API_URL$chemin"
    fi
}
cle() { sed -n "s/.*\"$1\":\"\\([^\"]*\\)\".*/\\1/p" "$TMP/out" | head -1; }
premier_id() { sed -n 's/.*"id":"\([^"]*\)".*/\1/p' "$TMP/out" | head -1; }

echo "== Jetons (flux OIDC reel)"
ADMIN=$(bash "$HERE/token.sh" "$PREFIX.admin" "$SMOKE_PASSWORD");     verifier "jeton ADMIN obtenu" 1 "$([ -n "$ADMIN" ] && echo 1 || echo 0)"
AGENT=$(bash "$HERE/token.sh" "$PREFIX.agent" "$SMOKE_PASSWORD");     verifier "jeton AGENT obtenu" 1 "$([ -n "$AGENT" ] && echo 1 || echo 0)"
VALID=$(bash "$HERE/token.sh" "$PREFIX.validateur" "$SMOKE_PASSWORD"); verifier "jeton VALIDATEUR obtenu" 1 "$([ -n "$VALID" ] && echo 1 || echo 0)"
CONSU=$(bash "$HERE/token.sh" "$PREFIX.consultant" "$SMOKE_PASSWORD"); verifier "jeton CONSULTANT obtenu" 1 "$([ -n "$CONSU" ] && echo 1 || echo 0)"
[ -n "$ADMIN$AGENT$VALID$CONSU" ] && [ -n "$ADMIN" ] && [ -n "$AGENT" ] && [ -n "$VALID" ] && [ -n "$CONSU" ] || { echo "Jetons manquants : arret."; exit 2; }

echo "== Authentification et audience"
verifier "sans jeton -> 401" 401 "$($C -o /dev/null -w '%{http_code}' "$API_URL/referentiels/statuts-judiciaires")"
verifier "jeton falsifie -> 401" 401 "$(http "${ADMIN}x" GET /referentiels/statuts-judiciaires)"
SVC=$($C -X POST "$AUTH_URL/realms/logintegrite/protocol/openid-connect/token" -d grant_type=client_credentials \
      -d client_id=logintegrite-admin-api --data-urlencode "client_secret=${KEYCLOAK_ADMIN_CLIENT_SECRET:-}" | sed -n 's/.*"access_token":"\([^"]*\)".*/\1/p')
if [ -n "$SVC" ]; then verifier "jeton signe par Keycloak mais SANS l'audience de l'API -> 401" 401 "$(http "$SVC" GET /referentiels/statuts-judiciaires)"
else echo "  [IGNORE] KEYCLOAK_ADMIN_CLIENT_SECRET non fourni : test d'audience non execute"; fi

echo "== Autorisations par role"
verifier "CONSULTANT lit les referentiels" 200 "$(http "$CONSU" GET /referentiels/statuts-judiciaires)"
verifier "CONSULTANT ne peut pas exporter" 403 "$(http "$CONSU" GET /rapports/personnes/excel)"
verifier "CONSULTANT n'a pas les statistiques" 403 "$(http "$CONSU" GET /statistiques/globales)"
verifier "CONSULTANT ne peut pas creer de categorie" 403 "$(http "$CONSU" POST /referentiels/categories-infraction '{"libelle":"x"}')"
verifier "AGENT n'accede pas a l'audit" 403 "$(http "$AGENT" GET /audit/forensique/etat-chaine)"
verifier "route inconnue -> 404" 404 "$(http "$ADMIN" GET /inconnu)"

echo "== Parcours metier : ouverture d'un dossier"
http "$ADMIN" GET /referentiels/sources-signalement >/dev/null; SRC=$(premier_id)
http "$ADMIN" GET /referentiels/roles-implication >/dev/null;   ROLE=$(premier_id)
http "$ADMIN" GET /referentiels/types-infraction >/dev/null;    TYPE=$(premier_id)
http "$ADMIN" GET /referentiels/statuts-judiciaires >/dev/null; STATUT=$(premier_id)
http "$ADMIN" GET /referentiels/types-document >/dev/null;      TDOC=$(premier_id)
verifier "referentiels de depart presents" 1 "$([ -n "$SRC" ] && [ -n "$ROLE" ] && [ -n "$TYPE" ] && [ -n "$STATUT" ] && [ -n "$TDOC" ] && echo 1 || echo 0)"
NOM="Smoke$(date +%s)"
CORPS=$(cat <<EOF
{"nouvellePersonnePhysique":{"nomNaissance":"$NOM","prenoms":"Test","sexe":"M","dateNaissance":"1970-03-02","profession":"Directeur"},
 "dossier":{"intitule":"Dossier de test $NOM","sourceSignalementId":"$SRC","dateOuverture":"2026-09-01"},
 "roleImplicationId":"$ROLE","fonctionOccupee":"Directeur",
 "premierFait":{"typeInfractionId":"$TYPE","dateFaits":"2026-06-15","description":"Fait de test","montantPrejudice":25000000}}
EOF
)
verifier "CONSULTANT ne peut pas ouvrir de dossier" 403 "$(http "$CONSU" POST /dossiers/ouverture "$CORPS")"
verifier "AGENT ouvre un dossier complet" 201 "$(http "$AGENT" POST /dossiers/ouverture "$CORPS")"
PERSONNE=$(cle personneId); DOSSIER=$(cle dossierId); IMPLICATION=$(cle implicationId); FAIT=$(cle premierFaitId)
verifier "identifiants renvoyes" 1 "$([ -n "$PERSONNE" ] && [ -n "$DOSSIER" ] && [ -n "$IMPLICATION" ] && [ -n "$FAIT" ] && echo 1 || echo 0)"
verifier "le dossier est consultable" 200 "$(http "$AGENT" GET "/dossiers/$DOSSIER")"

echo "== Validation du fait"
verifier "AGENT ne valide pas son propre fait" 403 "$(http "$AGENT" PUT "/faits/$FAIT/valider")"
verifier "VALIDATEUR valide le fait" 200 "$(http "$VALID" PUT "/faits/$FAIT/valider")"
verifier "la personne apparait au registre officiel" 200 "$(http "$CONSU" GET "/personnes/recherche?page=0&size=50&statutAncrage=REGISTRE_OFFICIEL")"
contient "le registre officiel contient la personne validee" "$TMP/out" "$NOM"

echo "== Statut judiciaire et peine"
verifier "AGENT lie la personne au fait avec un statut" 201 "$(http "$AGENT" POST "/implications/$IMPLICATION/liaisons-faits" "{\"faitReprocheId\":\"$FAIT\",\"statutJudiciaireId\":\"$STATUT\"}")"
LIAISON=$(cle id)
verifier "AGENT enregistre une peine" 201 "$(http "$AGENT" POST "/liaisons-faits/$LIAISON/peines" '{"typePeine":"AMENDE","montantAmende":1000000,"dateDecision":"2026-08-30","description":"Amende de test"}')"
verifier "CONSULTANT ne peut pas enregistrer de peine" 403 "$(http "$CONSU" POST "/liaisons-faits/$LIAISON/peines" '{"typePeine":"AMENDE","montantAmende":1}')"

echo "== Documents"
printf '%%PDF-1.4\n1 0 obj<<>>endobj\ntrailer<<>>\n%%%%EOF\n' > "$TMP/piece.pdf"
printf 'MZ contenu executable' > "$TMP/malware.exe"
printf '<script>alert(1)</script>' > "$TMP/faux.pdf"
upload() { $C -o "$TMP/out" -w '%{http_code}' -X POST -H "Authorization: Bearer $AGENT" -F "typeDocumentId=$TDOC" -F "fichier=@$TMPF/$1;filename=$2" "$API_URL/dossiers/$DOSSIER/documents"; }
verifier "PDF valide accepte" 201 "$(upload piece.pdf piece.pdf)"
DOC=$(cle id); HASH=$(cle hashIntegrite)
verifier "executable .exe refuse" 400 "$(upload malware.exe malware.exe)"
verifier "faux PDF (contenu incoherent) refuse" 400 "$(upload faux.pdf faux.pdf)"
verifier "nom avec chemin neutralise (accepte, stocke sans chemin)" 201 "$(upload piece.pdf '../../etc/piece2.pdf')"
$C -o "$TMP/dl.bin" -D "$TMP/dl.h" -H "Authorization: Bearer $AGENT" "$API_URL/dossiers/$DOSSIER/documents/$DOC/telecharger"
verifier "telechargement : contenu identique au fichier depose" "$(sha256sum "$TMP/piece.pdf" | cut -d' ' -f1)" "$(sha256sum "$TMP/dl.bin" | cut -d' ' -f1)"
verifier "telechargement : empreinte d'integrite enregistree = SHA-256 reel" "$(sha256sum "$TMP/piece.pdf" | cut -d' ' -f1)" "$HASH"
contient "telechargement : en-tete nosniff" "$TMP/dl.h" "nosniff"
http "$ADMIN" GET /dossiers >/dev/null; AUTRE=$(grep -o '"id":"[^"]*"' "$TMP/out" | sed 's/"id":"//; s/"//' | grep -v "$DOSSIER" | head -1)
if [ -n "$AUTRE" ]; then verifier "document demande via un autre dossier -> 404" 404 "$($C -o /dev/null -w '%{http_code}' -H "Authorization: Bearer $AGENT" "$API_URL/dossiers/$AUTRE/documents/$DOC/telecharger")"; fi

echo "== Rapports et statistiques"
verifier "AGENT exporte le PDF du dossier" 200 "$($C -o "$TMP/r.pdf" -w '%{http_code}' -H "Authorization: Bearer $AGENT" "$API_URL/rapports/dossiers/$DOSSIER/pdf")"
verifier "le rapport est bien un PDF" "%PDF" "$(head -c 4 "$TMP/r.pdf")"
verifier "AGENT exporte l'Excel des personnes" 200 "$(http "$AGENT" GET /rapports/personnes/excel)"
verifier "registre officiel : PDF genere" 200 "$($C -o "$TMP/registre.pdf" -w '%{http_code}' -H "Authorization: Bearer $AGENT" "$API_URL/rapports/registre-officiel/pdf")"
verifier "registre officiel : c'est bien un PDF" "%PDF" "$(head -c 4 "$TMP/registre.pdf")"
verifier "Excel des dossiers genere" 200 "$($C -o "$TMP/dossiers.xlsx" -w '%{http_code}' -H "Authorization: Bearer $AGENT" "$API_URL/rapports/dossiers/excel")"
verifier "Excel des dossiers : c'est bien un classeur" "PK" "$(head -c 2 "$TMP/dossiers.xlsx")"
if [ -n "${SMOKE_KEEP:-}" ]; then cp "$TMP/registre.pdf" "$TMP/dossiers.xlsx" "$SMOKE_KEEP/"; fi
verifier "tableau de bord ADMIN" 200 "$(http "$ADMIN" GET /statistiques/dashboard-executif)"

echo "== Audit forensique"
verifier "etat de la chaine (ADMIN)" 200 "$(http "$ADMIN" GET /audit/forensique/etat-chaine)"
TOTAL=$(sed -n 's/.*"totalEntrees":\([0-9]*\).*/\1/p' "$TMP/out")
verifier "des actions ont ete journalisees (>= 6)" 1 "$([ "${TOTAL:-0}" -ge 6 ] && echo 1 || echo 0)"
verifier "verification de la chaine" 200 "$(http "$ADMIN" POST /audit/forensique/verifier-chaine)"
contient "la chaine d'audit est intacte" "$TMP/out" '"chaineIntegre":true'
verifier "indicateurs forensiques" 200 "$(http "$ADMIN" GET /audit/forensique/kpi)"
contient "les consultations sont journalisees" "$TMP/out" '"consultations24h":[1-9]'

echo
echo "Resultat : $OK controles reussis, $KO en echec"
[ "$KO" -eq 0 ]
