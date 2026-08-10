-- ============================================================================
-- LOG INTEGRITE - ASCE-LC
-- Script DDL PostgreSQL 15+
-- Genere a partir du dictionnaire de donnees v2 (26 tables)
-- Convention : snake_case, cles primaires UUID v7, schemas par domaine metier
--
-- IMPORTANT : ce script n'a pas pu etre execute dans le present environnement
-- (pas d'acces reseau pour installer un serveur PostgreSQL de test).
-- Il a ete redige et relu avec soin mais DOIT etre rejoue une premiere fois
-- sur un PostgreSQL 15+ vierge avant toute mise en production.
-- ============================================================================

BEGIN;

-- ============================================================================
-- 0. EXTENSIONS
-- ============================================================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;   -- gen_random_bytes(), digest() (sha256)
CREATE EXTENSION IF NOT EXISTS pg_trgm;    -- recherche floue (GIN) sur les noms

-- ============================================================================
-- 1. SCHEMAS (un par domaine metier - cf. decoupage DDD retenu)
-- ============================================================================
CREATE SCHEMA IF NOT EXISTS core;          -- fonctions/utilitaires transverses
CREATE SCHEMA IF NOT EXISTS securite;      -- utilisateurs, roles, habilitations
CREATE SCHEMA IF NOT EXISTS referentiels;  -- listes administrables
CREATE SCHEMA IF NOT EXISTS personnes;     -- personne physique/morale
CREATE SCHEMA IF NOT EXISTS dossiers;      -- dossier, implication, fait, peine
CREATE SCHEMA IF NOT EXISTS documents;     -- pieces jointes
CREATE SCHEMA IF NOT EXISTS audit;         -- journal d'audit et de consultation
CREATE SCHEMA IF NOT EXISTS systeme;       -- parametrage applicatif

-- ============================================================================
-- 2. FONCTION UTILITAIRE : GENERATION UUID v7
-- (PostgreSQL 15/16/17 n'ont pas de uuidv7() natif - ajoute en PG18.
--  Si vous deployez sur PG18+, vous pouvez remplacer les appels a
--  core.uuid_generate_v7() par la fonction native uuidv7().)
-- ============================================================================
CREATE OR REPLACE FUNCTION core.uuid_generate_v7()
RETURNS uuid
LANGUAGE plpgsql
VOLATILE
AS $$
DECLARE
    ts_millis   bigint := (extract(epoch FROM clock_timestamp()) * 1000)::bigint;
    uuid_bytes  bytea;
BEGIN
    -- 6 octets (48 bits) d'horodatage big-endian + 10 octets aleatoires
    uuid_bytes := substring(int8send(ts_millis) FROM 3 FOR 6) || gen_random_bytes(10);
    -- Version (7) sur les 4 bits de poids fort de l'octet 6
    uuid_bytes := set_byte(uuid_bytes, 6, (get_byte(uuid_bytes, 6) & 15) | 112);
    -- Variant RFC 4122 (10xx) sur les 2 bits de poids fort de l'octet 8
    uuid_bytes := set_byte(uuid_bytes, 8, (get_byte(uuid_bytes, 8) & 63) | 128);
    RETURN encode(uuid_bytes, 'hex')::uuid;
END;
$$;

COMMENT ON FUNCTION core.uuid_generate_v7() IS
'Genere un UUID v7 (time-ordered) : evite la fragmentation des index B-Tree
observee avec des UUID v4 purement aleatoires sur de gros volumes.';

-- Fonction generique de maintenance des colonnes date_maj
CREATE OR REPLACE FUNCTION core.maj_date_modification()
RETURNS TRIGGER AS $$
BEGIN
    NEW.date_maj := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Fonction generique bloquant UPDATE/DELETE (donnees a valeur probante)
CREATE OR REPLACE FUNCTION core.empecher_modification()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Table %.% : modification/suppression interdite (donnee immuable, valeur probante)',
        TG_TABLE_SCHEMA, TG_TABLE_NAME;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- 3. TYPES ENUMERES (etats structurels fixes, non administrables)
-- ============================================================================
CREATE TYPE core.type_personne          AS ENUM ('PHYSIQUE', 'MORALE');
CREATE TYPE core.sexe                   AS ENUM ('M', 'F');
CREATE TYPE core.situation_matrimoniale AS ENUM ('CELIBATAIRE', 'MARIE', 'DIVORCE', 'VEUF');
CREATE TYPE core.type_piece_identite    AS ENUM ('CNIB', 'PASSEPORT', 'NIF');
CREATE TYPE core.statut_personne_morale AS ENUM ('ACTIVE', 'DISSOUTE', 'RADIEE', 'EN_LIQUIDATION');
CREATE TYPE core.statut_dossier         AS ENUM ('OUVERT', 'CLOTURE');
CREATE TYPE core.statut_validation      AS ENUM ('EN_ATTENTE', 'VALIDEE', 'REJETEE');
CREATE TYPE core.type_peine             AS ENUM ('PRISON', 'AMENDE', 'CONFISCATION', 'RADIATION', 'AUTRE');
CREATE TYPE core.nature_sanction        AS ENUM ('JUDICIAIRE', 'ADMINISTRATIVE');
CREATE TYPE core.niveau_zone            AS ENUM ('PAYS', 'REGION', 'PROVINCE', 'COMMUNE');
CREATE TYPE core.niveau_entite          AS ENUM ('MINISTERE', 'DIRECTION', 'SERVICE');
CREATE TYPE core.code_role              AS ENUM ('ADMIN', 'AGENT', 'VALIDATEUR', 'CONSULTANT');

-- ============================================================================
-- 4. SCHEMA SECURITE
-- (cree avant "personnes" car personne.cree_par_id y fait reference)
-- ============================================================================
CREATE TABLE securite.utilisateur (
    id              UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    keycloak_id     TEXT NOT NULL,
    -- cache synchronise depuis Keycloak (source de verite = Keycloak,
    -- rafraichi a chaque connexion) ; evite un appel externe a chaque
    -- affichage de rapport/journal d'audit.
    nom             TEXT NOT NULL,
    prenom          TEXT NOT NULL,
    email           TEXT NOT NULL,
    telephone       TEXT,
    actif           BOOLEAN NOT NULL DEFAULT true,
    date_creation   TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_maj        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_utilisateur_keycloak_id UNIQUE (keycloak_id),
    CONSTRAINT uq_utilisateur_email UNIQUE (email)
);
CREATE TRIGGER trg_utilisateur_maj BEFORE UPDATE ON securite.utilisateur
    FOR EACH ROW EXECUTE FUNCTION core.maj_date_modification();

CREATE TABLE securite.role_habilitation (
    id                          UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    code                        core.code_role NOT NULL,
    libelle                     TEXT NOT NULL,
    -- Permission DISTINCTE du role : autorise ou non la vue globale d'un
    -- dossier (toutes personnes/faits confondus) plutot que la vue filtree
    -- par personne, qui reste le mode de consultation par defaut.
    acces_vue_globale_dossier   BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT uq_role_habilitation_code UNIQUE (code)
);

CREATE TABLE securite.utilisateur_role (
    id                      UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    utilisateur_id          UUID NOT NULL REFERENCES securite.utilisateur(id),
    role_habilitation_id    UUID NOT NULL REFERENCES securite.role_habilitation(id),
    date_attribution        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_utilisateur_role UNIQUE (utilisateur_id, role_habilitation_id)
);
CREATE INDEX idx_utilisateur_role_utilisateur ON securite.utilisateur_role(utilisateur_id);
CREATE INDEX idx_utilisateur_role_role ON securite.utilisateur_role(role_habilitation_id);

-- ============================================================================
-- 5. SCHEMA REFERENTIELS
-- ============================================================================
CREATE TABLE referentiels.role_implication (
    id      UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    libelle TEXT NOT NULL,
    actif   BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT uq_role_implication_libelle UNIQUE (libelle)
);

CREATE TABLE referentiels.statut_judiciaire (
    id      UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    libelle TEXT NOT NULL,
    actif   BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT uq_statut_judiciaire_libelle UNIQUE (libelle)
);

CREATE TABLE referentiels.categorie_infraction (
    id          UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    libelle     TEXT NOT NULL,
    description TEXT,
    CONSTRAINT uq_categorie_infraction_libelle UNIQUE (libelle)
);

CREATE TABLE referentiels.type_infraction (
    id                      UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    categorie_infraction_id UUID NOT NULL REFERENCES referentiels.categorie_infraction(id),
    libelle                 TEXT NOT NULL,
    actif                   BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT uq_type_infraction_libelle UNIQUE (libelle)
);
CREATE INDEX idx_type_infraction_categorie ON referentiels.type_infraction(categorie_infraction_id);
CREATE INDEX idx_type_infraction_actif ON referentiels.type_infraction(categorie_infraction_id) WHERE actif = true;

CREATE TABLE referentiels.source_signalement (
    id          UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    libelle     TEXT NOT NULL,
    description TEXT,
    CONSTRAINT uq_source_signalement_libelle UNIQUE (libelle)
);

CREATE TABLE referentiels.type_document (
    id      UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    libelle TEXT NOT NULL,
    actif   BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT uq_type_document_libelle UNIQUE (libelle)
);

CREATE TABLE referentiels.zone_geographique (
    id          UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    libelle     TEXT NOT NULL,
    niveau      core.niveau_zone NOT NULL,
    parent_id   UUID REFERENCES referentiels.zone_geographique(id),
    code        TEXT
);
CREATE INDEX idx_zone_geographique_parent ON referentiels.zone_geographique(parent_id);

CREATE TABLE referentiels.entite_organisation (
    id          UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    libelle     TEXT NOT NULL,
    niveau      core.niveau_entite NOT NULL,
    parent_id   UUID REFERENCES referentiels.entite_organisation(id)
);
CREATE INDEX idx_entite_organisation_parent ON referentiels.entite_organisation(parent_id);

-- ============================================================================
-- 6. SCHEMA PERSONNES
-- ============================================================================
CREATE TABLE personnes.personne (
    id              UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    type_personne   core.type_personne NOT NULL,
    nom_affichage   TEXT NOT NULL,
    date_creation   TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_maj        TIMESTAMPTZ NOT NULL DEFAULT now(),
    cree_par_id     UUID NOT NULL REFERENCES securite.utilisateur(id),
    version         BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_personne_cree_par ON personnes.personne(cree_par_id);
-- Recherche floue (tolerante aux fautes de frappe/variantes orthographiques)
CREATE INDEX idx_personne_nom_trgm ON personnes.personne USING gin (nom_affichage gin_trgm_ops);
CREATE TRIGGER trg_personne_maj BEFORE UPDATE ON personnes.personne
    FOR EACH ROW EXECUTE FUNCTION core.maj_date_modification();

COMMENT ON TABLE personnes.personne IS
'Regle metier applicative (non exprimable en contrainte SQL pure) : une personne
ne doit jamais etre creee sans une premiere ligne dossiers.implication associee.
A garantir par une transaction unique cote service Spring (creerPersonneEtImplication).';

CREATE TABLE personnes.personne_physique (
    id                          UUID PRIMARY KEY REFERENCES personnes.personne(id),
    nom_naissance               TEXT NOT NULL,
    nom_usage                   TEXT,
    prenoms                     TEXT NOT NULL,
    sexe                        core.sexe NOT NULL,
    date_naissance              DATE,
    lieu_naissance              TEXT,
    nationalite                 TEXT NOT NULL,
    nom_pere                    TEXT,
    nom_mere                    TEXT,
    situation_matrimoniale      core.situation_matrimoniale,
    nom_conjoint                TEXT,
    profession                  TEXT,
    matricule_fonction_publique TEXT,
    grade_categorie             TEXT,
    adresse                     TEXT,
    telephone                   TEXT,
    email                       TEXT,
    photo_url                   TEXT
);

CREATE TABLE personnes.personne_morale (
    id                          UUID PRIMARY KEY REFERENCES personnes.personne(id),
    denomination_sociale        TEXT NOT NULL,
    sigle                       TEXT,
    forme_juridique             TEXT NOT NULL,
    rccm                        TEXT,
    ifu                         TEXT,
    capital_social              NUMERIC(18,2),
    secteur_activite            TEXT NOT NULL,
    siege_social                TEXT NOT NULL,
    telephone                   TEXT,
    email                       TEXT,
    logo_url                    TEXT,
    date_creation_entreprise    DATE,
    statut                      core.statut_personne_morale NOT NULL DEFAULT 'ACTIVE',
    representant_legal_id       UUID REFERENCES personnes.personne_physique(id)
);
CREATE INDEX idx_personne_morale_representant ON personnes.personne_morale(representant_legal_id);

CREATE TABLE personnes.piece_identite (
    id                      UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    personne_physique_id    UUID NOT NULL REFERENCES personnes.personne_physique(id),
    type_piece              core.type_piece_identite NOT NULL,
    numero                  TEXT NOT NULL,
    date_delivrance         DATE,
    date_expiration         DATE,
    -- Verrou anti-doublon (Q53) : deux personnes ne peuvent pas partager
    -- le meme numero de piece du meme type. C'est la premiere ligne de
    -- defense automatique contre la creation d'un doublon de Personne.
    CONSTRAINT uq_piece_identite_numero UNIQUE (type_piece, numero)
);
CREATE INDEX idx_piece_identite_personne ON personnes.piece_identite(personne_physique_id);

CREATE TABLE personnes.alias (
    id              UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    personne_id     UUID NOT NULL REFERENCES personnes.personne(id),
    nom_alias       TEXT NOT NULL,
    commentaire     TEXT
);
CREATE INDEX idx_alias_personne ON personnes.alias(personne_id);
CREATE INDEX idx_alias_nom_trgm ON personnes.alias USING gin (nom_alias gin_trgm_ops);

-- ============================================================================
-- 7. SCHEMA DOSSIERS (coeur du modele)
-- ============================================================================
CREATE TABLE dossiers.dossier (
    id                      UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    numero_dossier          TEXT,
    intitule                TEXT,
    source_signalement_id   UUID NOT NULL REFERENCES referentiels.source_signalement(id),
    statut_dossier          core.statut_dossier NOT NULL DEFAULT 'OUVERT',
    date_ouverture          DATE NOT NULL DEFAULT current_date,
    date_cloture            DATE,
    description_contexte    TEXT,
    -- Champ JSONB volontairement UNIQUE et etroitement scope (futures
    -- references judiciaires externes : n de parquet, juridiction saisie...).
    -- Ne jamais y faire glisser une donnee qui devrait etre une colonne.
    metadonnees_externes    JSONB,
    date_creation           TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_maj                TIMESTAMPTZ NOT NULL DEFAULT now(),
    version                 BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_dossier_dates CHECK (date_cloture IS NULL OR date_cloture >= date_ouverture)
);
CREATE INDEX idx_dossier_source ON dossiers.dossier(source_signalement_id);
CREATE INDEX idx_dossier_numero ON dossiers.dossier(numero_dossier);
CREATE INDEX idx_dossier_metadonnees ON dossiers.dossier USING gin (metadonnees_externes);
CREATE TRIGGER trg_dossier_maj BEFORE UPDATE ON dossiers.dossier
    FOR EACH ROW EXECUTE FUNCTION core.maj_date_modification();

CREATE TABLE dossiers.implication (
    id                          UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    personne_id                 UUID NOT NULL REFERENCES personnes.personne(id),
    dossier_id                  UUID NOT NULL REFERENCES dossiers.dossier(id),
    role_implication_id         UUID NOT NULL REFERENCES referentiels.role_implication(id),
    entite_organisation_id      UUID REFERENCES referentiels.entite_organisation(id),
    fonction_occupee            TEXT,
    -- Snapshot fige, independant du referentiel, pour garder l'exactitude
    -- historique meme si l'entite est renommee/restructuree plus tard.
    entite_libelle_a_l_epoque   TEXT,
    date_debut                  DATE NOT NULL DEFAULT current_date,
    date_fin                    DATE,
    observations                TEXT,
    date_creation               TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_implication_personne_dossier UNIQUE (personne_id, dossier_id),
    CONSTRAINT ck_implication_dates CHECK (date_fin IS NULL OR date_fin >= date_debut)
);
CREATE INDEX idx_implication_personne ON dossiers.implication(personne_id);
CREATE INDEX idx_implication_dossier ON dossiers.implication(dossier_id);
CREATE INDEX idx_implication_role ON dossiers.implication(role_implication_id);
CREATE INDEX idx_implication_entite ON dossiers.implication(entite_organisation_id);

CREATE TABLE dossiers.fait_reproche (
    id                          UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    dossier_id                  UUID NOT NULL REFERENCES dossiers.dossier(id),
    type_infraction_id          UUID NOT NULL REFERENCES referentiels.type_infraction(id),
    zone_geographique_id        UUID REFERENCES referentiels.zone_geographique(id),
    date_faits                  DATE NOT NULL,
    lieu_precis                 TEXT,
    description                 TEXT NOT NULL,
    montant_prejudice           NUMERIC(18,2) NOT NULL,
    devise                      CHAR(3) NOT NULL DEFAULT 'XOF',
    montant_confirme_justice    NUMERIC(18,2),
    statut_validation           core.statut_validation NOT NULL DEFAULT 'EN_ATTENTE',
    motif_rejet                 TEXT,
    valide_par_id                UUID REFERENCES securite.utilisateur(id),
    date_validation              TIMESTAMPTZ,
    date_creation                TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_maj                     TIMESTAMPTZ NOT NULL DEFAULT now(),
    version                      BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_fait_montant_positif CHECK (montant_prejudice >= 0),
    CONSTRAINT ck_fait_montant_justice_positif CHECK (montant_confirme_justice IS NULL OR montant_confirme_justice >= 0),
    CONSTRAINT ck_fait_devise CHECK (devise ~ '^[A-Z]{3}$')
);
CREATE INDEX idx_fait_reproche_dossier ON dossiers.fait_reproche(dossier_id);
CREATE INDEX idx_fait_reproche_type ON dossiers.fait_reproche(type_infraction_id);
CREATE INDEX idx_fait_reproche_zone ON dossiers.fait_reproche(zone_geographique_id);
CREATE INDEX idx_fait_reproche_valide_par ON dossiers.fait_reproche(valide_par_id);
-- Index partiel : la file de travail "faits en attente de validation" est
-- une requete tres frequente du Validateur - on ne veut indexer que ce sous-ensemble.
CREATE INDEX idx_fait_reproche_en_attente ON dossiers.fait_reproche(dossier_id)
    WHERE statut_validation = 'EN_ATTENTE';
CREATE TRIGGER trg_fait_reproche_maj BEFORE UPDATE ON dossiers.fait_reproche
    FOR EACH ROW EXECUTE FUNCTION core.maj_date_modification();

CREATE TABLE dossiers.implication_fait (
    id                      UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    implication_id          UUID NOT NULL REFERENCES dossiers.implication(id),
    fait_reproche_id        UUID NOT NULL REFERENCES dossiers.fait_reproche(id),
    statut_judiciaire_id    UUID NOT NULL REFERENCES referentiels.statut_judiciaire(id),
    date_statut             DATE NOT NULL DEFAULT current_date,
    commentaire             TEXT,
    date_creation           TIMESTAMPTZ NOT NULL DEFAULT now(),
    date_maj                TIMESTAMPTZ NOT NULL DEFAULT now(),
    version                 BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_implication_fait UNIQUE (implication_id, fait_reproche_id)
);
CREATE INDEX idx_implication_fait_implication ON dossiers.implication_fait(implication_id);
CREATE INDEX idx_implication_fait_fait ON dossiers.implication_fait(fait_reproche_id);
CREATE INDEX idx_implication_fait_statut ON dossiers.implication_fait(statut_judiciaire_id);
CREATE TRIGGER trg_implication_fait_maj BEFORE UPDATE ON dossiers.implication_fait
    FOR EACH ROW EXECUTE FUNCTION core.maj_date_modification();

CREATE TABLE dossiers.peine (
    id                      UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    implication_fait_id     UUID NOT NULL REFERENCES dossiers.implication_fait(id),
    type_peine              core.type_peine NOT NULL,
    nature_sanction         core.nature_sanction,
    duree                   TEXT,
    montant_amende          NUMERIC(18,2),
    date_decision           DATE,
    date_execution          DATE,
    description             TEXT,
    date_creation           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_peine_montant_positif CHECK (montant_amende IS NULL OR montant_amende >= 0),
    CONSTRAINT ck_peine_dates CHECK (date_execution IS NULL OR date_decision IS NULL OR date_execution >= date_decision)
);
CREATE INDEX idx_peine_implication_fait ON dossiers.peine(implication_fait_id);

-- ============================================================================
-- 8. SCHEMA DOCUMENTS
-- ============================================================================
CREATE TABLE documents.document (
    id                  UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    dossier_id          UUID NOT NULL REFERENCES dossiers.dossier(id),
    type_document_id    UUID NOT NULL REFERENCES referentiels.type_document(id),
    nom_original        TEXT NOT NULL,
    nom_stockage        TEXT NOT NULL,
    chemin_stockage     TEXT NOT NULL,
    type_mime           TEXT NOT NULL,
    taille_octets       BIGINT NOT NULL,
    hash_integrite      TEXT NOT NULL,
    immuable            BOOLEAN NOT NULL DEFAULT true,
    uploade_par_id      UUID NOT NULL REFERENCES securite.utilisateur(id),
    date_upload         TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_document_taille_positive CHECK (taille_octets > 0)
);
CREATE INDEX idx_document_dossier ON documents.document(dossier_id);
CREATE INDEX idx_document_type ON documents.document(type_document_id);
CREATE INDEX idx_document_uploade_par ON documents.document(uploade_par_id);

-- Table de liaison OPTIONNELLE : tague un document a une/des implication(s)
-- specifique(s). Un document non tague reste "global au dossier", visible
-- dans toutes les vues filtrees par personne (comportement par defaut,
-- rien ne se perd). Voir dossiers.documents_visibles_pour_personne() plus bas.
CREATE TABLE documents.document_implication (
    document_id     UUID NOT NULL REFERENCES documents.document(id),
    implication_id  UUID NOT NULL REFERENCES dossiers.implication(id),
    date_creation   TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (document_id, implication_id)
);
CREATE INDEX idx_document_implication_implication ON documents.document_implication(implication_id);

-- ============================================================================
-- 9. SCHEMA SYSTEME
-- ============================================================================
CREATE TABLE systeme.parametre_systeme (
    id          UUID PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    cle         TEXT NOT NULL,
    valeur      TEXT NOT NULL,
    description TEXT,
    date_maj    TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_parametre_systeme_cle UNIQUE (cle)
);

-- ============================================================================
-- 10. SCHEMA AUDIT (tables PARTITIONNEES par annee - fort volume attendu)
-- ============================================================================
CREATE TABLE audit.journal_audit (
    id                  UUID NOT NULL DEFAULT core.uuid_generate_v7(),
    utilisateur_id      UUID REFERENCES securite.utilisateur(id),
    action               TEXT NOT NULL,
    entite_cible         TEXT NOT NULL,
    entite_cible_id      UUID,
    valeur_avant         JSONB,
    valeur_apres         JSONB,
    hash_precedent       TEXT NOT NULL,
    hash_actuel          TEXT NOT NULL,
    date_action          TIMESTAMPTZ NOT NULL DEFAULT now(),
    adresse_ip           TEXT,
    PRIMARY KEY (id, date_action)   -- la cle de partition doit figurer dans la PK
) PARTITION BY RANGE (date_action);

CREATE INDEX idx_journal_audit_utilisateur ON audit.journal_audit(utilisateur_id);
CREATE INDEX idx_journal_audit_entite ON audit.journal_audit(entite_cible, entite_cible_id);
CREATE INDEX idx_journal_audit_valeur_avant ON audit.journal_audit USING gin (valeur_avant);
CREATE INDEX idx_journal_audit_valeur_apres ON audit.journal_audit USING gin (valeur_apres);

-- Partitions annuelles (a etendre chaque annee - ou automatiser via pg_partman)
CREATE TABLE audit.journal_audit_2025 PARTITION OF audit.journal_audit
    FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');
CREATE TABLE audit.journal_audit_2026 PARTITION OF audit.journal_audit
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');
CREATE TABLE audit.journal_audit_2027 PARTITION OF audit.journal_audit
    FOR VALUES FROM ('2027-01-01') TO ('2028-01-01');
-- Filet de securite : toute date hors bornes definies atterrit ici plutot
-- que de faire echouer l'insertion (a surveiller/vider periodiquement).
CREATE TABLE audit.journal_audit_default PARTITION OF audit.journal_audit DEFAULT;

CREATE TABLE audit.journal_consultation (
    id                      UUID NOT NULL DEFAULT core.uuid_generate_v7(),
    utilisateur_id          UUID NOT NULL REFERENCES securite.utilisateur(id),
    entite_consultee        TEXT NOT NULL,
    entite_consultee_id     UUID NOT NULL,
    date_consultation       TIMESTAMPTZ NOT NULL DEFAULT now(),
    adresse_ip              TEXT,
    PRIMARY KEY (id, date_consultation)
) PARTITION BY RANGE (date_consultation);

CREATE INDEX idx_journal_consultation_utilisateur ON audit.journal_consultation(utilisateur_id);
CREATE INDEX idx_journal_consultation_entite ON audit.journal_consultation(entite_consultee, entite_consultee_id);

CREATE TABLE audit.journal_consultation_2025 PARTITION OF audit.journal_consultation
    FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');
CREATE TABLE audit.journal_consultation_2026 PARTITION OF audit.journal_consultation
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');
CREATE TABLE audit.journal_consultation_2027 PARTITION OF audit.journal_consultation
    FOR VALUES FROM ('2027-01-01') TO ('2028-01-01');
CREATE TABLE audit.journal_consultation_default PARTITION OF audit.journal_consultation DEFAULT;

COMMENT ON TABLE audit.journal_consultation IS
'Separee de journal_audit pour une raison de performance, pas seulement
semantique : une simple lecture peut survenir des dizaines de fois plus
souvent qu''une modification, et journal_audit est chaine cryptographiquement
(chaque ligne depend de la precedente), ce qui serialise les ecritures.
Volontairement NON chainee ici par defaut (a activer via un trigger
identique a celui de journal_audit si une valeur probante est requise
meme sur la simple consultation).';

-- ============================================================================
-- 11. CHAINAGE CRYPTOGRAPHIQUE DU JOURNAL D'AUDIT
-- ============================================================================
CREATE OR REPLACE FUNCTION audit.calculer_chainage_audit()
RETURNS TRIGGER AS $$
DECLARE
    dernier_hash TEXT;
BEGIN
    -- Verrou transactionnel : evite que deux insertions concurrentes
    -- lisent le meme "dernier hash" et cassent la chaine. A l'echelle
    -- (des milliers d'ecritures/s), ce point de serialisation devra etre
    -- reevalue (ex. chaine par lot/minute plutot que par ligne).
    PERFORM pg_advisory_xact_lock(hashtext('journal_audit_chain'));

    SELECT hash_actuel INTO dernier_hash
    FROM audit.journal_audit
    ORDER BY date_action DESC
    LIMIT 1;

    NEW.hash_precedent := COALESCE(dernier_hash, repeat('0', 64));
    NEW.hash_actuel := encode(
        digest(
            NEW.hash_precedent
            || COALESCE(NEW.utilisateur_id::text, '')
            || NEW.action
            || NEW.entite_cible
            || COALESCE(NEW.entite_cible_id::text, '')
            || COALESCE(NEW.valeur_avant::text, '')
            || COALESCE(NEW.valeur_apres::text, '')
            || NEW.date_action::text,
            'sha256'
        ),
        'hex'
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_chainage_audit
    BEFORE INSERT ON audit.journal_audit
    FOR EACH ROW EXECUTE FUNCTION audit.calculer_chainage_audit();

-- ============================================================================
-- 12. IMMUABILITE (defense en profondeur : meme un acces SQL direct
-- ne peut pas contourner la regle, pas seulement la couche applicative)
-- ============================================================================
CREATE TRIGGER trg_document_immuable
    BEFORE UPDATE OR DELETE ON documents.document
    FOR EACH ROW EXECUTE FUNCTION core.empecher_modification();

CREATE TRIGGER trg_journal_audit_immuable
    BEFORE UPDATE OR DELETE ON audit.journal_audit
    FOR EACH ROW EXECUTE FUNCTION core.empecher_modification();

CREATE TRIGGER trg_journal_consultation_immuable
    BEFORE DELETE ON audit.journal_consultation
    FOR EACH ROW EXECUTE FUNCTION core.empecher_modification();

-- Protection semi-souple d'un fait deja valide : on peut le faire evoluer
-- en le repassant explicitement a EN_ATTENTE (nouveau cycle de validation,
-- cf. Q4 : seul CE fait redevient en attente, pas tout le dossier), mais
-- on ne peut pas modifier silencieusement son contenu en restant VALIDEE.
CREATE OR REPLACE FUNCTION dossiers.proteger_fait_valide()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.statut_validation = 'VALIDEE' AND NEW.statut_validation = 'VALIDEE' THEN
        IF NEW.montant_prejudice IS DISTINCT FROM OLD.montant_prejudice
           OR NEW.description IS DISTINCT FROM OLD.description
           OR NEW.date_faits IS DISTINCT FROM OLD.date_faits
           OR NEW.type_infraction_id IS DISTINCT FROM OLD.type_infraction_id THEN
            RAISE EXCEPTION
                'Fait % deja valide : repasser par un nouveau cycle (statut_validation = EN_ATTENTE) pour le modifier',
                OLD.id;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_proteger_fait_valide
    BEFORE UPDATE ON dossiers.fait_reproche
    FOR EACH ROW EXECUTE FUNCTION dossiers.proteger_fait_valide();

-- ============================================================================
-- 13. FONCTIONS DE CONSULTATION FILTREE PAR PERSONNE
-- (traduction directe, en SQL reel, de la demonstration validee sur le
-- scenario "Kabre Jean" - Ecrans 1/2/3)
-- ============================================================================

-- ECRAN 1 : recherche -> une ligne par personne x dossier (jamais de doublon
-- de personne pour plusieurs faits d'un meme dossier)
CREATE OR REPLACE FUNCTION personnes.rechercher_personne(p_terme TEXT)
RETURNS TABLE (
    personne_id     UUID,
    nom_affichage   TEXT,
    dossier_id      UUID,
    dossier_intitule TEXT,
    role            TEXT
) LANGUAGE sql STABLE AS $$
    SELECT p.id, p.nom_affichage, d.id, d.intitule, r.libelle
    FROM personnes.personne p
    JOIN dossiers.implication i ON i.personne_id = p.id
    JOIN dossiers.dossier d ON d.id = i.dossier_id
    JOIN referentiels.role_implication r ON r.id = i.role_implication_id
    WHERE p.nom_affichage ILIKE '%' || p_terme || '%';
$$;

-- ECRAN 2 : faits reproches concernant SPECIFIQUEMENT cette personne
-- dans ce dossier (grace au pivot implication_fait)
CREATE OR REPLACE FUNCTION dossiers.faits_visibles_pour_personne(p_dossier_id UUID, p_personne_id UUID)
RETURNS TABLE (
    fait_id             UUID,
    description         TEXT,
    montant_prejudice   NUMERIC,
    statut_judiciaire   TEXT
) LANGUAGE sql STABLE AS $$
    SELECT fr.id, fr.description, fr.montant_prejudice, sj.libelle
    FROM dossiers.implication i
    JOIN dossiers.implication_fait ifa ON ifa.implication_id = i.id
    JOIN dossiers.fait_reproche fr ON fr.id = ifa.fait_reproche_id
    JOIN referentiels.statut_judiciaire sj ON sj.id = ifa.statut_judiciaire_id
    WHERE i.dossier_id = p_dossier_id AND i.personne_id = p_personne_id;
$$;

-- ECRAN 3 (corrige) : documents visibles = ceux non tagues (globaux au
-- dossier) + ceux tagues specifiquement a cette personne
CREATE OR REPLACE FUNCTION dossiers.documents_visibles_pour_personne(p_dossier_id UUID, p_personne_id UUID)
RETURNS SETOF documents.document
LANGUAGE sql STABLE AS $$
    SELECT doc.*
    FROM documents.document doc
    WHERE doc.dossier_id = p_dossier_id
      AND (
            NOT EXISTS (SELECT 1 FROM documents.document_implication di WHERE di.document_id = doc.id)
         OR EXISTS (
                SELECT 1
                FROM documents.document_implication di
                JOIN dossiers.implication i ON i.id = di.implication_id
                WHERE di.document_id = doc.id AND i.personne_id = p_personne_id
            )
      );
$$;

-- ============================================================================
-- 14. ROW-LEVEL SECURITY - SCAFFOLDING OPTIONNEL, NON ACTIVE
-- (le cloisonnement des agents par region/juridiction n'a pas ete tranche
-- avec le porteur - active seulement si confirme necessaire)
-- ============================================================================
-- ALTER TABLE dossiers.fait_reproche ENABLE ROW LEVEL SECURITY;
-- CREATE POLICY fait_reproche_par_region ON dossiers.fait_reproche
--     USING (zone_geographique_id = ANY (
--         string_to_array(current_setting('log_integrite.zones_autorisees', true), ',')::uuid[]
--     ));

-- ============================================================================
-- 15. DONNEES DE REFERENCE (seed) - valeurs discutees et validees
-- ============================================================================
INSERT INTO securite.role_habilitation (code, libelle, acces_vue_globale_dossier) VALUES
    ('ADMIN', 'Administrateur', true),
    ('AGENT', 'Agent', false),
    ('VALIDATEUR', 'Validateur', true),
    ('CONSULTANT', 'Consultant partenaire', false);

INSERT INTO referentiels.role_implication (libelle) VALUES
    ('Auteur principal'), ('Complice'), ('Corrupteur'), ('Corrompu'),
    ('Societe beneficiaire'), ('Receleur'), ('Prete-nom'), ('Temoin');

INSERT INTO referentiels.categorie_infraction (libelle) VALUES
    ('Financiere'),
    ('Marches publics'),
    ('Administrative et gouvernance'),
    ('Economique'),
    ('Autre');

INSERT INTO referentiels.statut_judiciaire (libelle) VALUES
    ('Enquete'), ('Instruction'), ('Poursuite'), ('Jugement'),
    ('Condamnation'), ('Relaxe'), ('Acquittement'), ('Classement sans suite'),
    ('Appel'), ('Cassation'), ('Prescription');

INSERT INTO referentiels.source_signalement (libelle) VALUES
    ('Plainte'), ('Auto-saisine'), ('Denonciation'),
    ('Signalement d''un autre corps de controle'), ('Presse');

INSERT INTO referentiels.type_document (libelle) VALUES
    ('Rapport d''audit'), ('PV d''audition'), ('Decision de justice'),
    ('Releve bancaire'), ('Correspondance'), ('Photographie'), ('Autre');

INSERT INTO referentiels.zone_geographique (libelle, niveau) VALUES
    ('Burkina Faso', 'PAYS');

-- Utilisateur systeme temporaire - le temps de brancher la vraie resolution
-- de l'utilisateur courant via le JWT Keycloak. A NE PAS garder en
-- production ; sert uniquement a debloquer les tests de creation de
-- Personne pendant qu'on construit le backend module par module.
INSERT INTO securite.utilisateur (keycloak_id, nom, prenom, email) VALUES
    ('system-init', 'Systeme', 'Init', 'system@asce-lc.bf');

COMMIT;
