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

## 1. Certificat TLS et secrets
**Certificat acheté (GoDaddy)** : copiez sur le serveur qui héberge la pile les trois fichiers (`.crt`, chaîne `gd_bundle-g2-g1.crt`, clé `.key`), puis :
```bash
scripts/preparer-certificat.sh asce-lc.bf.crt gd_bundle-g2-g1.crt asce-lc.bf.key     # crée certs/fullchain.pem et certs/privkey.pem
scripts/generer-secrets.sh certificat                                                 # au lieu d'un e-mail Let's Encrypt
```
Le script vérifie que la clé correspond au certificat, que `logintegrite.asce-lc.bf` est couvert et que le certificat n'est pas expiré.
**Attention** : un certificat émis pour `asce-lc.bf` seul ne couvre pas le sous-domaine ; il faut un certificat `*.asce-lc.bf` (joker) ou `logintegrite.asce-lc.bf`. Le dossier `certs/` n'est jamais versionné ; la clé n'a de sens que sur le serveur de production. Sans certificat acheté, utilisez `generer-secrets.sh <e-mail>` (Let's Encrypt).

### Secrets
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

### Copie hors serveur (chiffrée)
`scripts/copier-sauvegardes.sh` prend le **dernier** jeu, vérifie ses empreintes (une sauvegarde altérée n'est jamais
envoyée), le chiffre en AES-256, le copie vers une autre machine et purge les jeux anciens de la destination.
Les sauvegardes contiennent des données personnelles : elles ne quittent jamais le serveur en clair.

1. **Phrase secrète** (à conserver aussi HORS du serveur, par exemple dans un gestionnaire de mots de passe : sans elle,
   les copies sont inutilisables) :
   ```bash
   openssl rand -base64 32 > ~/sauvegarde.phrase && chmod 600 ~/sauvegarde.phrase
   ```
2. **Clé SSH dédiée** vers la machine de destination (sans mot de passe, réservée à cet usage) :
   ```bash
   ssh-keygen -t ed25519 -f ~/.ssh/id_sauvegarde -N ''
   ssh-copy-id -i ~/.ssh/id_sauvegarde.pub utilisateur@destination
   ```
3. **Réglages** à ajouter dans `.env.prod` :
   ```
   SAUVEGARDE_DEST=utilisateur@destination:/srv/sauvegardes/logintegrite/
   SAUVEGARDE_PASSPHRASE=/home/administrator/sauvegarde.phrase
   SAUVEGARDE_SSH_OPTIONS=-i /home/administrator/.ssh/id_sauvegarde
   SAUVEGARDE_CONSERVER=14
   ```
4. **Essai manuel**, puis planification quotidienne (après la sauvegarde du service, par exemple à 3 h) :
   ```bash
   scripts/copier-sauvegardes.sh
   ( crontab -l 2>/dev/null; echo '0 3 * * * cd /home/administrator/logintegrite_backend && scripts/copier-sauvegardes.sh >> /home/administrator/copie-sauvegardes.log 2>&1' ) | crontab -
   ```
5. **Restauration d'une copie** (sur la machine de votre choix) :
   ```bash
   openssl enc -d -aes-256-cbc -pbkdf2 -iter 200000 -pass file:sauvegarde.phrase \
       -in logintegrite_db-<horodatage>.dump.enc -out logintegrite_db-<horodatage>.dump
   sha256sum -c SHA256SUMS-<horodatage>      # après avoir déchiffré aussi le fichier SHA256SUMS-<horodatage>.enc
   ```
   Puis `restore.sh` (voir ci-dessus). **Un test de restauration complet au moins une fois est indispensable** : une sauvegarde
   jamais restaurée n'est pas une sauvegarde.

## 6. Ce que garantit cette configuration
| Sujet | Mesure |
|---|---|
| Transport | HTTPS partout, HSTS, redirections automatiques |
| Base de données | 4 rôles à moindre privilège ; l'application ne peut ni modifier le schéma, ni désactiver les triggers, ni modifier ou supprimer l'audit et les documents |
| Audit | chaîne de hachage SHA-256 et immuabilité par triggers, plus refus SQL au rôle applicatif |
| Numérotation | références officielles attribuées par la base (`PERS-2026-00001`, `ORG-…`, `DOSS-…`) : sans doublon ni trou, impossibles à imposer par le client ; le compteur est inaccessible au rôle applicatif (migration V7) |
| Jetons | signature, émetteur **et audience** (`logintegrite-api`) vérifiés |
| Keycloak | mode production, mots de passe forts, blocage après 5 échecs, console restreinte par réseau |
| Conteneurs | `no-new-privileges`, capacités retirées pour le backend, journaux avec rotation |

## 7. Points à traiter par l'exploitant
- Sauvegarde hors serveur et test de restauration périodique.
- Supervision et alertes (santé des conteneurs, espace disque, expiration des certificats).
- Bascule de `Content-Security-Policy-Report-Only` vers `Content-Security-Policy` après observation (Caddyfile).
- Rotation périodique des secrets ; `KEYCLOAK_ADMIN_CLIENT_SECRET` doit rester identique dans Keycloak et le backend.
- Mises à jour régulières des images (Keycloak, Postgres, Caddy, JRE, nginx).

## 3 bis. Mot de passe oublie
Le lien "Mot de passe oublie ?" est retire (`resetPasswordAllowed=false`) tant qu aucun serveur de messagerie (SMTP) n est configure dans Keycloak : sans lui, aucun e-mail ne partirait. Un administrateur reinitialise un mot de passe en recreant le compte avec `scripts/keycloak-users.sh`. Pour reactiver le lien : configurer le SMTP (console Keycloak, realm logintegrite, Realm settings, Email) puis `resetPasswordAllowed=true`.

## Journal des connexions et des événements du poste (à faire une fois en production)

L'écran « Audit des actions » lit les connexions, déconnexions et échecs de connexion dans Keycloak. Le compte technique
de l'application a besoin du droit de lecture des événements, et la durée de conservation doit couvrir les besoins d'enquête
(2 ans ici). Sur un Keycloak déjà installé, le fichier d'import n'est pas rejoué : appliquer ces deux commandes.

```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod exec -T keycloak sh -c '
/opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080/auth --realm master \
  --user "$KC_BOOTSTRAP_ADMIN_USERNAME" --password "$KC_BOOTSTRAP_ADMIN_PASSWORD" &&
/opt/keycloak/bin/kcadm.sh add-roles -r logintegrite --uusername service-account-logintegrite-admin-api \
  --cclientid realm-management --rolename view-events &&
/opt/keycloak/bin/kcadm.sh update events/config -r logintegrite -s eventsEnabled=true -s eventsExpiration=63072000 \
  -s "enabledEventTypes=[\"LOGIN\",\"LOGOUT\",\"LOGIN_ERROR\"]" &&
echo OK'
```

Les événements du poste (tentatives de copie, d'impression, de capture) sont écrits dans `audit.journal_securite` (migration V8),
table en ajout seulement : ni modification ni suppression, même par l'application.

Limites à connaître : un navigateur ne peut pas empêcher une capture d'écran ni une photo de l'écran. Le dispositif repose
sur la dissuasion (filigrane au nom de l'utilisateur, masque quand la fenêtre perd le focus, copie et impression bloquées)
et sur la traçabilité (journal des tentatives). Un blocage réel des captures sur téléphone demande une application native
ou la gestion des appareils de l'organisation.

## Comptes de consultation (organismes extérieurs et institution)

Un compte avec le seul rôle `CONSULTANT` ne peut appeler QUE l'API de vérification (`/api/v1/verification/**`) et le
signalement d'événements de son poste. Tout le reste de l'API lui est refusé côté serveur (403), quelle que soit l'interface
utilisée : filtre `AccesConsultantFilter`, en liste blanche. Un compte qui cumule un autre rôle n'est pas concerné.

- **Recherche d'une personne précise seulement** : numéro de pièce, RCCM, IFU, numéro de personne, ou nom + prénoms + date de
  naissance. Pas de liste à parcourir. Cinq résultats au plus. Vingt recherches par dix minutes et par compte.
- **Registre officiel seulement** : une personne seulement « en instruction » n'apparaît pas, et son existence n'est pas révélée.
- **Fiche limitée** : dossiers entièrement validés, faits, statuts judiciaires (dont relaxes et acquittements) et peines.
  Aucun document, pièce d'identité, adresse ni historique interne.
- **Export sur demande** : le consultant dépose une demande motivée (écran « Mes demandes de dossier »). Un administrateur
  l'accorde ou la refuse (menu Administration > Demandes de dossier). Les demandes ne sont jamais supprimées (migration V9).
- **Traçabilité** : chaque recherche (critères et nombre de résultats), chaque fiche ouverte, chaque demande et chaque décision
  sont journalisés (onglet « Poste de travail » de l'audit et journal de consultation), avec l'adresse IP.

Pas d'expiration automatique des comptes : pour un organisme extérieur, désactiver le compte (Gestion des utilisateurs) à la
fin de la mission.
