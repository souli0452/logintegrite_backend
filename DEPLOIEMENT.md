# Déploiement de Log Intégrité (production)

Pile : Caddy (HTTPS) → frontend (nginx) · backend (Spring) · Keycloak (mode production) → PostgreSQL.
Seul Caddy publie des ports. La base n'est joignable que depuis le réseau Docker interne.

## Prérequis
- Un serveur avec Docker et Docker Compose v2, ports **80 et 443** ouverts.
- **Un seul nom DNS** : un enregistrement A `logintegrite.asce-lc.bf` vers l'adresse IP publique du serveur.
  Tout passe par ce nom : `/` (application), `/api/` (API), `/auth/` (connexion Keycloak). Un seul certificat, pas de CORS.
- Le dépôt frontend cloné à côté du backend (`../logintegrite_frontend`) ou `FRONTEND_CONTEXT` renseigné.

## 0. Branche à déployer
Le code de production se trouve sur la branche **`production`** des deux dépôts (`logintegrite_backend` et
`logintegrite_frontend`). Sur le serveur : `git clone -b production <dépôt>` pour chacun, côte à côte.

## 1. Secrets
```bash
scripts/generer-secrets.sh admin@asce-lc.bf            # e-mail Let's Encrypt ; domaine par défaut : logintegrite.asce-lc.bf
```
Le script crée `.env.prod` avec 7 secrets aléatoires distincts (jamais affichés) et refuse d'écraser un fichier existant.
**Sauvegardez `.env.prod` dans un coffre ou un gestionnaire de mots de passe** : le perdre, c'est perdre l'accès à la base
et à Keycloak. `.env.prod` n'est jamais versionné. **Ne réutilisez aucun secret de développement** : les anciens mots de
passe sont dans l'historique git du dépôt et doivent être considérés comme compromis.

## 2. Démarrage
```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build
docker compose -f docker-compose.prod.yml --env-file .env.prod ps     # tout doit être "healthy"
```
Au premier démarrage : création des rôles Postgres (`docker/postgres/init-prod.sh`), migrations Flyway
(rôle propriétaire), import du realm Keycloak (`docker/keycloak/import-prod`).

## 3. Premier administrateur
Le realm de production n'importe **aucun utilisateur**. Créez le premier ADMIN depuis un réseau autorisé :
```bash
KC_URL=https://logintegrite.asce-lc.bf/auth KC_ADMIN_PASSWORD=<KC_ADMIN_PASSWORD> \
  scripts/keycloak-users.sh create prenom.nom ADMIN '<mot de passe de 12+ caractères>' prenom.nom@asce-lc.bf
```
La console Keycloak (`/auth/admin`, realm `master`) n'est accessible que depuis `ADMIN_ALLOW` (réseaux privés par
défaut). Pour l'ouvrir : VPN, ou tunnel SSH. Politique de mots de passe : 12 caractères minimum,
majuscule, minuscule, chiffre.

## 4. Vérification après déploiement
Créer 4 comptes de test (ADMIN, AGENT, VALIDATEUR, CONSULTANT) préfixés `smoke.`, lancer le test de bout en bout,
puis les supprimer :
```bash
export API_URL=https://logintegrite.asce-lc.bf/api/v1 AUTH_URL=https://logintegrite.asce-lc.bf/auth APP_URL=https://logintegrite.asce-lc.bf
export SMOKE_PASSWORD='...' KEYCLOAK_ADMIN_CLIENT_SECRET='<valeur de .env.prod>'
scripts/smoke-test.sh      # ~47 contrôles : authentification, audience, rôles, parcours dossier, documents, audit
```
Le test crée des données réelles (une personne, un dossier) : à exécuter sur un environnement de recette,
ou à supprimer ensuite avec l'accord des responsables (l'audit, lui, est immuable par conception).

## 5. Sauvegardes
Le service `backup` produit chaque jour (`BACKUP_INTERVAL_SECONDS`) : base métier, base Keycloak, documents,
avec empreintes SHA-256, conservés `BACKUP_KEEP_DAYS` jours dans le volume `backups`.
**Copiez ce volume hors du serveur** (sauvegarde distante chiffrée) : un volume local ne protège pas d'une perte du serveur.

Restauration dans une base de vérification (sans toucher à la production) :
```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod exec backup /restore.sh           # dernière sauvegarde
```
Les empreintes sont vérifiées avant restauration. Testez la restauration régulièrement.

## 6. Ce que garantit cette configuration
| Sujet | Mesure |
|---|---|
| Transport | HTTPS partout, HSTS, redirections automatiques |
| Base de données | 4 rôles à moindre privilège ; l'application ne peut ni modifier le schéma, ni désactiver les triggers, ni modifier ou supprimer l'audit et les documents |
| Audit | chaîne de hachage SHA-256 et immuabilité par triggers, plus refus SQL au rôle applicatif |
| Jetons | signature, émetteur **et audience** (`logintegrite-api`) vérifiés |
| Keycloak | mode production, mots de passe forts, blocage après 5 échecs, console restreinte par réseau |
| Conteneurs | `no-new-privileges`, capacités retirées pour le backend, journaux avec rotation |

## 7. Points à traiter par l'exploitant
- Sauvegarde hors serveur et test de restauration périodique.
- Supervision et alertes (santé des conteneurs, espace disque, expiration des certificats).
- Bascule de `Content-Security-Policy-Report-Only` vers `Content-Security-Policy` après observation (Caddyfile).
- Rotation périodique des secrets ; `KEYCLOAK_ADMIN_CLIENT_SECRET` doit rester identique dans Keycloak et le backend.
- Mises à jour régulières des images (Keycloak, Postgres, Caddy, JRE, nginx).
