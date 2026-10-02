# Guide de déploiement de A à Z — Log Intégrité

Ce guide explique, étape par étape, comment mettre en ligne une application comme **Log Intégrité** (frontend Angular,
backend Spring Boot, Keycloak, PostgreSQL, HTTPS), comment la vérifier, la sauvegarder, la mettre à jour et la dépanner.
Il est écrit à partir du déploiement réel sur le serveur `log-int` de l'ASCE-LC, **problèmes rencontrés compris**.

Il complète `DEPLOIEMENT.md` (référence technique courte). En cas de doute entre les deux, `DEPLOIEMENT.md` prime.

---

## Comment lire ce guide

- **[Serveur]** : à taper sur le serveur (connexion SSH ou console de la machine virtuelle).
- **[Poste]** : à taper sur votre ordinateur de travail (PowerShell sous Windows).
- **Vous devez voir** : le résultat attendu. S'il est différent, **arrêtez-vous** et allez au chapitre 14 (dépannage) avant de continuer.
- Ne collez jamais un mot de passe, une clé ou le contenu de `.env.prod` dans un message, un ticket ou un courriel.

Temps à prévoir pour un premier déploiement : une demi-journée si le réseau et le certificat sont prêts. Les délais
réels viennent presque toujours de l'équipe réseau (pare-feu) et de l'obtention du certificat : commencez par eux.

---

## 1. Vue d'ensemble : ce que vous allez installer

```
   Internet / réseau de l'institution
                │  HTTPS (443)  — HTTP (80) redirigé vers HTTPS
                ▼
        ┌──────────────┐
        │    Caddy     │  seul service qui publie des ports ; certificat TLS ; en-têtes de sécurité
        └──────┬───────┘
     /         │ /api/*            │ /auth/*
     ▼         ▼                   ▼
 ┌──────────┐ ┌──────────┐   ┌──────────────┐
 │ frontend │ │ backend  │   │   Keycloak   │  connexion, comptes, rôles, journal des connexions
 │ (nginx)  │ │ (Spring) │   │ (production) │
 └──────────┘ └────┬─────┘   └──────┬───────┘
                   └───────┬────────┘
                           ▼
                  ┌─────────────────┐        ┌──────────────┐
                  │   PostgreSQL    │◄───────│   backup     │  sauvegarde quotidienne chiffrable
                  │ (réseau interne)│        └──────────────┘
                  └─────────────────┘
```

| Élément | Rôle | Nom du conteneur |
|---|---|---|
| Caddy | Reçoit tout le trafic, gère HTTPS, répartit vers les autres services | `lip-caddy` |
| frontend | Sert l'application Angular (fichiers statiques) | `lip-frontend` |
| backend | API : règles métier, droits, audit | `lip-backend` |
| Keycloak | Authentification (mots de passe, jetons, rôles) | `lip-keycloak` |
| PostgreSQL | Données de l'application et de Keycloak | `lip-postgres` |
| backup | Sauvegarde quotidienne de la base et des documents | `lip-backup` |

**Un seul nom de domaine** (`logintegrite.asce-lc.bf`) sert tout : `/` l'application, `/api/` l'API, `/auth/` la connexion.
Un seul certificat, aucun problème de CORS.

**Deux réseaux Docker** : `edge` (en contact avec Caddy) et `interne` (**sans accès à Internet**, réservé à la base).
La base de données n'est donc **jamais** joignable de l'extérieur.

**Deux dépôts**, côte à côte sur le serveur, branche **`production`** :
- `logintegrite_backend` : le code de l'API **et** toute la configuration de déploiement (`docker-compose.prod.yml`, Caddy,
  scripts, migrations de base de données) ;
- `logintegrite_frontend` : l'application Angular.

---

## 2. Avant de commencer : la liste de contrôle

| À avoir | Détail | Qui |
|---|---|---|
| Un serveur | Ubuntu Server LTS, 4 Go de mémoire minimum (8 Go recommandés), 40 Go de disque minimum. Ces tailles sont des **recommandations**, à ajuster à la charge réelle. | Infrastructure |
| Un nom de domaine | Ex. `logintegrite.asce-lc.bf` | DNS |
| Une adresse IP | Fixe, sur le réseau du serveur | Réseau |
| Un certificat TLS | Couvrant exactement le nom de domaine (voir chapitre 6) | Vous / achat |
| Des règles de pare-feu | Ports 80 et 443 **entrants** vers le serveur ; port 22 pour l'administration | **Équipe réseau** |
| L'accès aux dépôts | Comptes GitHub qui peuvent cloner les deux dépôts | Vous |
| Un coffre à mots de passe | Pour `.env.prod` et la phrase de chiffrement des sauvegardes | Vous |

**Commencez dès le jour 1 par les demandes à l'équipe réseau et au DNS** : ce sont les étapes les plus lentes.

---

## 3. Préparer le serveur [Serveur]

### 3.1 Installer et mettre à jour le système
Installez Ubuntu Server LTS, créez un utilisateur d'administration (ici `administrator`), puis :
```bash
sudo apt update && sudo apt upgrade -y
```

### 3.2 Régler l'heure (important)
Les jetons de connexion (JWT) ont une durée de vie courte : un serveur à l'heure fausse refuse des connexions valides.
```bash
timedatectl
```
Vous devez voir `System clock synchronized: yes`. Sinon : `sudo timedatectl set-ntp true`.

### 3.3 Pare-feu local (ufw)
```bash
sudo ufw allow 22/tcp      # administration
sudo ufw allow 80/tcp      # HTTP (redirigé vers HTTPS)
sudo ufw allow 443/tcp     # HTTPS
sudo ufw enable
sudo ufw status
```
Vous devez voir `Status: active` et les trois règles. **Autorisez le port 22 avant `enable`**, sinon vous vous coupez l'accès.

### 3.4 Installer Docker
Suivez la documentation officielle (https://docs.docker.com/engine/install/ubuntu/), puis :
```bash
sudo usermod -aG docker $USER      # puis déconnectez-vous et reconnectez-vous
docker --version
docker compose version
docker run --rm hello-world
```
Vous devez voir les versions, puis « Hello from Docker! ».
Si vous obtenez `permission denied while trying to connect to the docker API`, vous n'avez pas encore rouvert votre session
après `usermod`.

---

## 4. Le réseau : DNS et pare-feu de l'institution

### 4.1 DNS
Créez un enregistrement **A** : `logintegrite.asce-lc.bf` → adresse IP du serveur.
Pour **tester avant** que le DNS soit en place, ajoutez sur votre poste (en administrateur) une ligne dans le fichier
`C:\Windows\System32\drivers\etc\hosts` :
```
10.10.20.24   logintegrite.asce-lc.bf
```
(remplacez par l'adresse de votre serveur). Retirez-la quand le DNS est créé.

### 4.2 Pare-feu de l'institution (Fortinet ou autre)
C'est ici que se sont perdus le plus de jours lors du déploiement réel. Demandez par écrit :

> Autoriser TCP **80** et **443** depuis les réseaux des utilisateurs vers l'adresse du serveur, et TCP **22** uniquement
> depuis les postes d'administration. Merci de confirmer le numéro de règle créé.

**Comment vérifier, depuis votre poste** :
```powershell
Test-NetConnection 10.10.20.24 -Port 443
Test-NetConnection 10.10.20.24 -Port 80
Test-NetConnection 10.10.20.24 -Port 22
```
- `TcpTestSucceeded : True` → c'est ouvert.
- `PingSucceeded : True` mais `TcpTestSucceeded : False` → le chemin réseau existe, **un pare-feu bloque le port**. Ce n'est
  pas un problème du serveur.
- Dans le navigateur, `ERR_CONNECTION_TIMED_OUT` est le symptôme classique d'un pare-feu qui bloque sans répondre.

**Pour convaincre l'équipe réseau**, joignez vos résultats, et si possible la preuve qu'un **autre** serveur du même réseau
répond (comme `10.10.20.28` dans notre cas) : cela prouve que seule la règle pour votre serveur manque.

---

## 5. Récupérer le code [Serveur]

```bash
cd ~
git clone -b production https://github.com/souli0452/logintegrite_backend.git
git clone -b production https://github.com/souli0452/logintegrite_frontend.git
ls
```
Vous devez voir côte à côte `logintegrite_backend` et `logintegrite_frontend`. Le fichier `docker-compose.prod.yml` attend le
frontend dans `../logintegrite_frontend` (sinon renseignez `FRONTEND_CONTEXT` dans `.env.prod`).

Si le dépôt est privé, GitHub demandera une authentification : utilisez un **jeton d'accès personnel** en lecture seule, ou
une clé de déploiement. Ne laissez pas un mot de passe personnel sur le serveur.

Toutes les commandes suivantes se tapent **dans `~/logintegrite_backend`**.

---

## 6. Le certificat TLS

### 6.1 Ce qu'il faut
Un certificat qui **couvre exactement** le nom de domaine. Un certificat émis pour `asce-lc.bf` seul **ne couvre pas**
`logintegrite.asce-lc.bf` : il faut un certificat « joker » `*.asce-lc.bf` ou un certificat pour le sous-domaine.
Vous recevez trois fichiers : le certificat (`.crt`), la chaîne intermédiaire (ex. `gd_bundle-g2-g1.crt`) et la clé privée (`.key`).

### 6.2 Les envoyer sur le serveur [Poste]
```powershell
scp asce-lc.bf.crt gd_bundle-g2-g1.crt asce-lc.bf.key administrator@10.10.20.24:~/
```
La **clé privée** ne doit exister que sur le serveur et dans votre coffre. Ne l'envoyez jamais par courriel.

### 6.3 Les préparer [Serveur]
```bash
chmod +x scripts/*.sh
scripts/preparer-certificat.sh ~/asce-lc.bf.crt ~/gd_bundle-g2-g1.crt ~/asce-lc.bf.key
```
Le script vérifie : la clé correspond au certificat, le nom de domaine est couvert, la date de fin de validité. Il crée
`certs/fullchain.pem` et `certs/privkey.pem`. **Vous devez voir** un message de succès avec la date de fin (`notAfter`).

**Notez la date d'expiration dans un agenda, avec un rappel 30 jours avant.** Un certificat expiré rend le site inaccessible
(avertissement de sécurité dans tous les navigateurs).

### 6.4 Sans certificat acheté
Let's Encrypt (gratuit) est possible si le serveur est joignable depuis Internet sur le port 80 : passez une adresse de
courriel à `generer-secrets.sh` au chapitre suivant. Dans un réseau fermé, il faut un certificat acheté ou une autorité interne.

---

## 7. Les secrets [Serveur]

```bash
scripts/generer-secrets.sh certificat
```
(ou `scripts/generer-secrets.sh votre@courriel.bf` pour Let's Encrypt). Le script crée `.env.prod` avec **7 secrets aléatoires
distincts**, sans jamais les afficher, et **refuse d'écraser** un fichier existant.

Vérifiez qu'il ne reste aucun `change-me` :
```bash
grep -c "change-me" .env.prod
```
Vous devez voir `0`.

**Sauvegardez `.env.prod` dans votre coffre à mots de passe, tout de suite.** Le perdre, c'est perdre l'accès à la base et
à Keycloak. Il n'est jamais versionné dans Git.

> **Piège rencontré** : ne chargez pas ce fichier dans le shell avec `. ./.env.prod` ou `source .env.prod`. Une ligne contient
> deux chemins (`TLS_MODE=/certs/fullchain.pem /certs/privkey.pem`) et le shell répond `/certs/privkey.pem: No such file or
> directory`. Inoffensif, mais inutile : Docker lit le fichier lui-même avec `--env-file`.

| Variable | Rôle |
|---|---|
| `DOMAIN` | Nom de domaine unique |
| `TLS_MODE` | Certificat acheté (`/certs/fullchain.pem /certs/privkey.pem`), ou courriel Let's Encrypt |
| `ADMIN_ALLOW` | Réseaux autorisés à ouvrir la console Keycloak (`private_ranges` = réseaux privés) |
| `PG_ADMIN_PASSWORD`, `DB_OWNER_PASSWORD`, `DB_APP_PASSWORD`, `DB_BACKUP_PASSWORD`, `KC_DB_PASSWORD` | Quatre rôles PostgreSQL à moindre privilège, plus celui de Keycloak |
| `KC_ADMIN_PASSWORD` | Mot de passe du compte `admin` de la console Keycloak |
| `KEYCLOAK_ADMIN_CLIENT_SECRET` | Secret du compte technique que le backend utilise pour parler à Keycloak |
| `BACKUP_INTERVAL_SECONDS`, `BACKUP_KEEP_DAYS` | Fréquence et durée de conservation des sauvegardes locales |

---

## 8. Démarrer l'application [Serveur]

```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build
```
Cette commande construit les images (5 à 10 minutes la première fois) et démarre les six services. Puis :
```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod ps
```
**Vous devez voir** les six conteneurs `Up`, et `healthy` pour `lip-postgres`, `lip-backend` et `lip-frontend`.
Keycloak peut mettre une minute à devenir prêt.

Vérifiez les migrations de base de données :
```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod logs backend | grep -i "now at version"
```
Vous devez voir `now at version v10` (ou la dernière version de votre dépôt).

### Ce qui se passe au premier démarrage
1. PostgreSQL crée les quatre rôles à moindre privilège (script `docker/postgres/init-prod.sh`).
2. Keycloak importe le realm `logintegrite` (`docker/keycloak/import-prod`) : rôles, politique de mots de passe, clients.
3. Le backend applique les migrations de base de données (Flyway), avec un rôle propriétaire. En fonctionnement normal,
   l'application utilise un rôle **qui ne peut pas modifier le schéma**.
4. Le service de sauvegarde démarre.

| Migration | Contenu |
|---|---|
| V1–V4 | Schéma initial, chaîne d'audit, référentiels, valeurs par défaut |
| V5 | Droits à moindre privilège pour le rôle de l'application |
| V6 | Unicité des rôles par utilisateur |
| V7 | Numérotation officielle attribuée par la base (`PERS-2026-00001`…) |
| V8 | Journal des événements du poste (copie, impression, capture) |
| V9 | Demandes d'export de dossier |
| V10 | Date d'expiration des comptes |

---

## 9. Premier administrateur et réglages Keycloak [Serveur]

Le realm de production **n'importe aucun utilisateur** : vous créez le premier administrateur vous-même.
```bash
KC_URL=https://logintegrite.asce-lc.bf/auth KC_ADMIN_PASSWORD=<valeur de .env.prod> \
  scripts/keycloak-users.sh create prenom.nom ADMIN '<mot de passe de 12+ caractères>' prenom.nom@asce-lc.bf
```
Politique de mots de passe : 12 caractères minimum, une majuscule, une minuscule, un chiffre.

> Si l'adresse publique n'est pas encore joignable (pare-feu), utilisez `docker compose … exec keycloak` avec `kcadm.sh`
> à l'intérieur du conteneur : tout ce que fait la console peut se faire en ligne de commande.

### Deux types de comptes à ne pas confondre
| Compte | Où | Pour quoi |
|---|---|---|
| Votre compte administrateur (ex. `abdoul.drabo`) | realm `logintegrite` | **Utiliser l'application** |
| `admin` | realm `master` | **Administrer Keycloak** (console `/auth/admin`, réseaux autorisés seulement) |

Pour lister les comptes de l'application, sans afficher de secret :
```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod exec -T keycloak sh -c '
/opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080/auth --realm master \
  --user "$KC_BOOTSTRAP_ADMIN_USERNAME" --password "$KC_BOOTSTRAP_ADMIN_PASSWORD" >/dev/null &&
/opt/keycloak/bin/kcadm.sh get users -r logintegrite --fields username,email,enabled'
```

### Réglages à faire une fois
**1. Retirer « mot de passe oublié »** (tant qu'aucun serveur de messagerie n'est configuré) :
```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod exec -T keycloak sh -c '
/opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080/auth --realm master \
  --user "$KC_BOOTSTRAP_ADMIN_USERNAME" --password "$KC_BOOTSTRAP_ADMIN_PASSWORD" >/dev/null &&
/opt/keycloak/bin/kcadm.sh update realms/logintegrite -s resetPasswordAllowed=false && echo OK'
```
**2. Activer le journal des connexions** (conservation 2 ans) — commandes complètes dans `DEPLOIEMENT.md`, section
« Journal des connexions », à lancer une seule fois : elles donnent au backend le droit de lire les événements.

**À savoir si vous tapez ces commandes depuis Windows (Git Bash)** : préfixez-les par `MSYS_NO_PATHCONV=1`, sinon
Windows transforme `/opt/...` en `C:\Program Files\...`. Depuis le serveur Linux, ce n'est pas nécessaire.

---

## 10. Vérifier que tout fonctionne

### 10.1 Depuis le serveur
```bash
curl -sk -o /dev/null -w "%{http_code}\n" --resolve logintegrite.asce-lc.bf:443:127.0.0.1 https://logintegrite.asce-lc.bf/
curl -sk -o /dev/null -w "%{http_code}\n" --resolve logintegrite.asce-lc.bf:443:127.0.0.1 https://logintegrite.asce-lc.bf/api/actuator/health
```
Vous devez voir `200` puis `401` (l'API refuse un appel sans connexion : **c'est normal et voulu**).

> **Piège rencontré** : `curl https://localhost/` répond `000`. Caddy ne sert le certificat que pour le vrai nom de domaine ;
> avec « localhost » la connexion échoue. Utilisez toujours `--resolve` comme ci-dessus.

### 10.2 Depuis un poste de l'institution
1. Ouvrez `https://logintegrite.asce-lc.bf` : la page de connexion s'affiche, **sans alerte de sécurité** (cadenas fermé).
2. Connectez-vous avec le compte administrateur. Vous arrivez sur le tableau de bord.
3. Créez une personne : son numéro (`PERS-…`) est attribué par le serveur.
4. Dans « Audit des actions », l'onglet « Connexions » montre votre connexion.
5. Depuis un poste **hors réseau privé** : `https://logintegrite.asce-lc.bf/auth/admin` doit répondre **« Accès refusé »**.

### 10.3 Test de bout en bout (recommandé avant la mise en service)
Le script `scripts/smoke-test.sh` exécute environ 47 contrôles (authentification, rôles, parcours d'un dossier, documents,
audit). Voir `DEPLOIEMENT.md`, chapitre 4. Il crée de vraies données de test : exécutez-le de préférence sur un environnement de recette.

---

## 11. Les sauvegardes

Le service `backup` sauvegarde chaque jour la base de l'application, celle de Keycloak et les documents, avec empreintes
SHA-256. **Ces sauvegardes sont sur le même serveur : elles ne protègent pas d'une perte du serveur.**

Il faut donc deux choses, détaillées dans `DEPLOIEMENT.md`, chapitre 5 :
1. **Une copie chiffrée hors du serveur** (`scripts/copier-sauvegardes.sh`) : disque externe USB, ou autre machine.
   La **phrase de chiffrement** doit être conservée **ailleurs que sur le serveur** (coffre) : sans elle, les copies sont inutilisables.
2. **Un test de restauration complet, au moins une fois**, puis régulièrement : une sauvegarde jamais restaurée n'est pas
   une sauvegarde.

Avant toute mise à jour qui applique une migration, faites en plus une sauvegarde manuelle (chapitre 12).

---

## 12. Mettre à jour l'application (la routine à suivre à chaque fois) [Serveur]

```bash
cd ~/logintegrite_backend

# 1. Sauvegarde de la base AVANT toute chose (obligatoire si une migration est annoncée)
docker compose -f docker-compose.prod.yml --env-file .env.prod exec -T postgres \
  sh -c 'pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB"' > ~/avant-maj-$(date +%F).sql
ls -lh ~/avant-maj-*.sql          # le fichier ne doit pas être vide

# 2. Récupérer le nouveau code (les deux dépôts)
git pull origin production
cd ~/logintegrite_frontend && git pull origin production
cd ~/logintegrite_backend

# 3. Reconstruire et redémarrer
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d --build

# 4. Vérifier
docker compose -f docker-compose.prod.yml --env-file .env.prod ps
docker compose -f docker-compose.prod.yml --env-file .env.prod logs backend | grep -i "now at version"
curl -sk -o /dev/null -w "%{http_code}\n" --resolve logintegrite.asce-lc.bf:443:127.0.0.1 https://logintegrite.asce-lc.bf/
```
Le site est indisponible quelques secondes pendant le redémarrage : choisissez une heure creuse.

**Mettre à jour un seul des deux** : si seul le frontend a changé, `up -d --build frontend` suffit ; si seul le backend,
`up -d --build backend`.

### Si quelque chose tourne mal
- **Pas de migration dans la mise à jour** : revenez au code précédent (`git log` pour trouver le commit,
  `git checkout <commit>`), puis `up -d --build`.
- **Une migration a été appliquée** : ne revenez **pas** simplement au code précédent, la base a changé. Restaurez la
  sauvegarde prise à l'étape 1, après avoir **répété la procédure sur une copie** pour savoir qu'elle fonctionne.
  Cette procédure doit être testée **avant** d'en avoir besoin.

> **Piège rencontré** : `git pull` répond « Already up to date » et la construction est entièrement « CACHED ». Cela veut dire
> que le code était **déjà** à jour et déjà déployé, pas que la mise à jour a échoué. Pour vérifier :
> `git log --oneline -3` doit montrer le dernier commit attendu.

> **Piège rencontré** : `docker compose up -d --build` lancé depuis `~` répond `no configuration file provided: not found`.
> La commande se lance **dans `~/logintegrite_backend`**, avec `-f docker-compose.prod.yml --env-file .env.prod`.

---

## 13. L'exploitation au quotidien

| Fréquence | Tâche | Comment |
|---|---|---|
| Chaque jour | La sauvegarde automatique a bien tourné | `docker compose … logs backup \| tail` |
| Chaque semaine | Copie hors serveur à jour | Vérifier le journal de `copier-sauvegardes.sh` |
| Chaque semaine | Espace disque | `df -h` (alerte à 80 %) |
| Chaque mois | Conteneurs en bonne santé | `docker compose … ps` |
| Chaque mois | Journal d'audit : connexions échouées, tentatives de copie | Écran « Audit des actions » |
| Chaque mois | Comptes de consultation qui expirent bientôt | « Gestion des utilisateurs » (date en orange) |
| Chaque trimestre | Test de restauration | Voir chapitre 11 |
| Chaque trimestre | Mises à jour de sécurité du système | `sudo apt update && sudo apt upgrade` puis redémarrage |
| Chaque année | Renouvellement du certificat | Rappel 30 jours avant la date de fin |
| Selon besoin | Rotation des secrets | `KEYCLOAK_ADMIN_CLIENT_SECRET` doit rester identique dans Keycloak et dans `.env.prod` |

**Lire les journaux d'un service** :
```bash
docker compose -f docker-compose.prod.yml --env-file .env.prod logs --tail 100 backend
docker compose -f docker-compose.prod.yml --env-file .env.prod logs --tail 100 keycloak
docker compose -f docker-compose.prod.yml --env-file .env.prod logs --tail 100 caddy
```

---

## 14. Dépannage : symptôme, cause, remède

| Symptôme | Cause probable | Remède |
|---|---|---|
| Navigateur : `ERR_CONNECTION_TIMED_OUT` | Pare-feu de l'institution | `Test-NetConnection <ip> -Port 443` ; si `False` alors que le ping passe, demander la règle (chapitre 4) |
| `curl https://localhost/` répond `000` | Caddy ne sert que le vrai nom | Utiliser `--resolve` (chapitre 10) |
| Alerte de sécurité dans le navigateur | Certificat qui ne couvre pas le nom, ou expiré | Relancer `preparer-certificat.sh` ; vérifier `notAfter` |
| `permission denied … docker API` | Session non rouverte après `usermod -aG docker` | Se déconnecter puis se reconnecter |
| `no configuration file provided: not found` | Commande lancée hors du dossier du backend | `cd ~/logintegrite_backend`, avec `-f docker-compose.prod.yml --env-file .env.prod` |
| `/certs/privkey.pem: No such file or directory` | Fichier `.env.prod` chargé avec `source` | Ne pas le faire ; sans conséquence |
| `ufw status` : `inactive` | Pare-feu local non activé | `sudo ufw enable` (après avoir autorisé le port 22) |
| Échec de compilation du backend (classe manquante, ex. `FichierValidator`) | Un dossier versionné par erreur ou ignoré par Git (`storage/` dans `.gitignore`) | Corriger `.gitignore` (`/storage/` à la racine seulement) et recommiter les sources |
| `lip-backend` reste `unhealthy` | Keycloak pas prêt, ou secret incorrect | `logs backend` ; attendre une minute ; vérifier `KEYCLOAK_ADMIN_CLIENT_SECRET` |
| L'API répond `401` partout | Jeton refusé : heure du serveur fausse, ou audience incorrecte | `timedatectl` ; vérifier le realm |
| L'API répond `403` « Compte expiré » | La date d'expiration du compte est dépassée | « Gestion des utilisateurs » → menu du compte → « Modifier l'expiration » |
| Un consultant reçoit `403` partout | **Normal** : il n'a accès qu'à la vérification d'une personne | Voir `DEPLOIEMENT.md`, « Comptes de consultation » |
| Recherche de vérification : `429` | Plafond de 20 recherches par 10 minutes atteint | Attendre 10 minutes (protection voulue) |
| `/auth/admin` : « Accès refusé » depuis chez vous | Votre réseau n'est pas dans `ADMIN_ALLOW` | Ajouter votre réseau, ou utiliser `kcadm` dans le conteneur |
| Commande `kcadm` depuis Windows : `C:\Program Files\...` dans le chemin | Git Bash transforme `/opt/...` | Préfixer par `MSYS_NO_PATHCONV=1` |
| Ancien affichage après une mise à jour | Cache du navigateur | `Ctrl + Maj + R`, ou fenêtre de navigation privée |
| Le disque se remplit | Sauvegardes ou journaux | `df -h`, `docker system df` ; réduire `BACKUP_KEEP_DAYS` |

**En cas de doute, ne redémarrez pas tout au hasard** : lisez d'abord `ps` et les journaux du service concerné.
**N'utilisez jamais `docker compose down -v`** : l'option `-v` supprime les volumes, donc la base de données.

---

## 15. Liste de contrôle de sécurité avant la mise en service

- [ ] HTTPS partout, certificat valide, rappel de renouvellement en agenda.
- [ ] Seuls les ports 80, 443 (et 22 limité aux administrateurs) sont ouverts sur le serveur.
- [ ] `.env.prod` est dans le coffre, absent de Git, et lisible seulement par son propriétaire.
- [ ] Aucun secret de développement n'a été réutilisé en production.
- [ ] La base de données n'est pas joignable de l'extérieur (réseau `interne`).
- [ ] La console Keycloak n'est ouverte que depuis les réseaux autorisés (test hors réseau fait).
- [ ] Le compte `admin` de Keycloak a un mot de passe fort ; un compte administrateur nominatif existe.
- [ ] Aucun compte de test ne subsiste en production.
- [ ] Les comptes de consultation ont une date d'expiration.
- [ ] Les sauvegardes sont copiées **hors** du serveur, **chiffrées**, et une restauration a été **testée**.
- [ ] La phrase de chiffrement des sauvegardes est conservée **hors** du serveur.
- [ ] L'onglet « Connexions » de l'audit affiche bien les connexions.
- [ ] Le texte juridique affiché aux consultants a été validé par la direction.
- [ ] La base légale de la communication des données à des organismes extérieurs a été vérifiée avec le service juridique.

---

## 16. Adapter ce déploiement à une autre application du même type

La même architecture convient à toute application « frontend + API + authentification + base de données ». Ce qui change :

| À adapter | Où |
|---|---|
| Nom de domaine | `DOMAIN` dans `.env.prod`, certificat, DNS |
| Noms des dépôts et des branches | Chapitre 5 |
| Realm, clients et rôles Keycloak | `docker/keycloak/import-prod/*.json` (nom du realm, identifiant du client, rôles) |
| URL publiques du frontend | Arguments de construction du `Dockerfile` frontend (`API_URL`, `KEYCLOAK_URL`) |
| Rôles et mots de passe PostgreSQL | `docker/postgres/init-prod.sh` |
| Chemins de l'application | `docker/caddy/Caddyfile` (`/`, `/api/`, `/auth/`) |
| Migrations | `src/main/resources/db/migration` du backend |
| Limite de taille des envois | `request_body max_size` dans le Caddyfile (110 Mo ici) |

**Les principes à conserver, quelle que soit l'application** :
1. **Un seul point d'entrée** (Caddy) ; tout le reste sur des réseaux internes.
2. **Moindre privilège** : l'application n'a pas le droit de modifier le schéma ni d'effacer l'audit.
3. **Secrets générés, jamais écrits dans le code**, conservés dans un coffre.
4. **Droits vérifiés côté serveur**, jamais seulement masqués dans l'interface.
5. **Journaliser** ce qui touche aux données sensibles, dans une table qu'on ne peut pas modifier.
6. **Sauvegarder hors serveur, chiffrer, et tester la restauration.**
7. **Un déploiement se répète à l'identique** : tout passe par des scripts et des fichiers versionnés, pas par des gestes manuels.

---

## 17. Annexes

### Commandes utiles
```bash
# État des services
docker compose -f docker-compose.prod.yml --env-file .env.prod ps
# Redémarrer un seul service
docker compose -f docker-compose.prod.yml --env-file .env.prod restart backend
# Arrêter / relancer tout (SANS supprimer les données)
docker compose -f docker-compose.prod.yml --env-file .env.prod stop
docker compose -f docker-compose.prod.yml --env-file .env.prod up -d
# Espace utilisé par Docker
docker system df
# Version du schéma de base de données
docker compose -f docker-compose.prod.yml --env-file .env.prod logs backend | grep -i "now at version"
```

### Glossaire
| Terme | Signification |
|---|---|
| Conteneur | Un programme isolé avec tout ce dont il a besoin ; ici un par service |
| Docker Compose | L'outil qui démarre les six conteneurs ensemble à partir d'un fichier |
| Caddy | Le « portier » : reçoit tout le trafic et gère le certificat HTTPS |
| Keycloak | Le service qui gère les comptes, mots de passe et rôles |
| Realm | Un espace Keycloak (ici `logintegrite` pour l'application, `master` pour l'administration) |
| JWT / jeton | Le « badge » temporaire remis après connexion, que l'API vérifie |
| Migration | Une modification versionnée de la structure de la base, appliquée automatiquement |
| Flyway | L'outil qui applique les migrations |
| Pare-feu | L'équipement qui autorise ou bloque le trafic entre réseaux |
| `healthy` | Le conteneur a passé son contrôle de santé |

### Fichiers importants du dépôt backend
| Fichier | Rôle |
|---|---|
| `docker-compose.prod.yml` | Définition des six services, réseaux, volumes |
| `docker/caddy/Caddyfile` | Routage, HTTPS, en-têtes de sécurité, console Keycloak restreinte |
| `docker/postgres/init-prod.sh` | Création des rôles PostgreSQL à moindre privilège |
| `docker/keycloak/import-prod/` | Définition du realm de production (aucun utilisateur) |
| `docker/backup/` | Scripts de sauvegarde et de restauration |
| `scripts/generer-secrets.sh` | Génère `.env.prod` |
| `scripts/preparer-certificat.sh` | Prépare et vérifie le certificat |
| `scripts/keycloak-users.sh` | Crée un utilisateur Keycloak en ligne de commande |
| `scripts/copier-sauvegardes.sh` | Copie chiffrée des sauvegardes hors serveur |
| `scripts/smoke-test.sh` | Test de bout en bout après déploiement |
| `.env.prod.example` | Modèle commenté des réglages |
| `DEPLOIEMENT.md` | Référence technique courte |
