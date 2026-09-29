-- V1 : schema initial (types, fonction uuid v7, tables, contraintes, index).
-- Genere a partir du modele JPA ; les schemas sont crees par Flyway (spring.flyway.schemas).
--
--




--
-- Name: audit; Type: SCHEMA; Schema: -; Owner: -
--



--
-- Name: core; Type: SCHEMA; Schema: -; Owner: -
--



--
-- Name: documents; Type: SCHEMA; Schema: -; Owner: -
--



--
-- Name: dossiers; Type: SCHEMA; Schema: -; Owner: -
--



--
-- Name: personnes; Type: SCHEMA; Schema: -; Owner: -
--



--
-- Name: referentiels; Type: SCHEMA; Schema: -; Owner: -
--



--
-- Name: securite; Type: SCHEMA; Schema: -; Owner: -
--



--
-- Name: code_role; Type: TYPE; Schema: core; Owner: -
--

CREATE TYPE core.code_role AS ENUM (
    'ADMIN',
    'AGENT',
    'VALIDATEUR',
    'CONSULTANT'
);


--
-- Name: nature_sanction; Type: TYPE; Schema: core; Owner: -
--

CREATE TYPE core.nature_sanction AS ENUM (
    'JUDICIAIRE',
    'ADMINISTRATIVE'
);


--
-- Name: niveau_entite; Type: TYPE; Schema: core; Owner: -
--

CREATE TYPE core.niveau_entite AS ENUM (
    'MINISTERE',
    'DIRECTION',
    'SERVICE'
);


--
-- Name: niveau_zone; Type: TYPE; Schema: core; Owner: -
--

CREATE TYPE core.niveau_zone AS ENUM (
    'PAYS',
    'REGION',
    'PROVINCE',
    'COMMUNE'
);


--
-- Name: sexe; Type: TYPE; Schema: core; Owner: -
--

CREATE TYPE core.sexe AS ENUM (
    'M',
    'F'
);


--
-- Name: situation_matrimoniale; Type: TYPE; Schema: core; Owner: -
--

CREATE TYPE core.situation_matrimoniale AS ENUM (
    'CELIBATAIRE',
    'MARIE',
    'DIVORCE',
    'VEUF'
);


--
-- Name: statut_dossier; Type: TYPE; Schema: core; Owner: -
--

CREATE TYPE core.statut_dossier AS ENUM (
    'OUVERT',
    'CLOTURE'
);


--
-- Name: statut_personne_morale; Type: TYPE; Schema: core; Owner: -
--

CREATE TYPE core.statut_personne_morale AS ENUM (
    'ACTIVE',
    'DISSOUTE',
    'RADIEE',
    'EN_LIQUIDATION'
);


--
-- Name: statut_validation; Type: TYPE; Schema: core; Owner: -
--

CREATE TYPE core.statut_validation AS ENUM (
    'EN_ATTENTE',
    'VALIDEE',
    'REJETEE'
);


--
-- Name: type_peine; Type: TYPE; Schema: core; Owner: -
--

CREATE TYPE core.type_peine AS ENUM (
    'PRISON',
    'AMENDE',
    'CONFISCATION',
    'RADIATION',
    'AUTRE'
);


--
-- Name: type_personne; Type: TYPE; Schema: core; Owner: -
--

CREATE TYPE core.type_personne AS ENUM (
    'PHYSIQUE',
    'MORALE'
);


--
-- Name: type_piece_identite; Type: TYPE; Schema: core; Owner: -
--

CREATE TYPE core.type_piece_identite AS ENUM (
    'CNIB',
    'PASSEPORT',
    'NIF'
);


--
-- Name: uuid_generate_v7(); Type: FUNCTION; Schema: core; Owner: -
--

CREATE FUNCTION core.uuid_generate_v7() RETURNS uuid
    LANGUAGE sql
    AS $$
  SELECT encode(
    set_bit(set_bit(
      overlay(uuid_send(gen_random_uuid())
              placing substring(int8send((extract(epoch FROM clock_timestamp()) * 1000)::bigint) FROM 3)
              FROM 1 FOR 6),
      52, 1), 53, 1),
    'hex')::uuid;
$$;




--
-- Name: journal_audit; Type: TABLE; Schema: audit; Owner: -
--

CREATE TABLE audit.journal_audit (
    date_action timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    action character varying(255) NOT NULL,
    adresse_ip character varying(255),
    entite_cible character varying(255) NOT NULL,
    entite_cible_id uuid,
    hash_actuel character varying(255),
    hash_precedent character varying(255),
    valeur_apres jsonb,
    valeur_avant jsonb,
    utilisateur_id uuid
);


--
-- Name: journal_consultation; Type: TABLE; Schema: audit; Owner: -
--

CREATE TABLE audit.journal_consultation (
    date_consultation timestamp(6) with time zone NOT NULL,
    id uuid NOT NULL,
    adresse_ip character varying(255),
    entite_consultee character varying(255) NOT NULL,
    entite_consultee_id uuid NOT NULL,
    utilisateur_id uuid NOT NULL
);


--
-- Name: parametre_systeme; Type: TABLE; Schema: core; Owner: -
--

CREATE TABLE core.parametre_systeme (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    cle character varying(255) NOT NULL,
    description character varying(255),
    valeur character varying(255)
);


--
-- Name: document; Type: TABLE; Schema: documents; Owner: -
--

CREATE TABLE documents.document (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    chemin_stockage character varying(255) NOT NULL,
    date_upload timestamp(6) with time zone,
    hash_integrite character varying(255) NOT NULL,
    immuable boolean NOT NULL,
    nom_original character varying(255) NOT NULL,
    nom_stockage character varying(255) NOT NULL,
    taille_octets bigint NOT NULL,
    type_mime character varying(255) NOT NULL,
    dossier_id uuid NOT NULL,
    type_document_id uuid NOT NULL,
    uploade_par_id uuid NOT NULL
);


--
-- Name: document_implication; Type: TABLE; Schema: documents; Owner: -
--

CREATE TABLE documents.document_implication (
    date_creation timestamp(6) with time zone,
    document_id uuid NOT NULL,
    implication_id uuid NOT NULL
);


--
-- Name: dossier; Type: TABLE; Schema: dossiers; Owner: -
--

CREATE TABLE dossiers.dossier (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    date_creation timestamp(6) with time zone NOT NULL,
    date_maj timestamp(6) with time zone NOT NULL,
    version bigint NOT NULL,
    date_cloture date,
    date_ouverture date NOT NULL,
    description_contexte text,
    intitule character varying(255),
    numero_dossier character varying(255),
    statut_dossier core.statut_dossier NOT NULL,
    source_signalement_id uuid NOT NULL
);


--
-- Name: fait_reproche; Type: TABLE; Schema: dossiers; Owner: -
--

CREATE TABLE dossiers.fait_reproche (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    date_creation timestamp(6) with time zone NOT NULL,
    date_maj timestamp(6) with time zone NOT NULL,
    version bigint NOT NULL,
    date_faits date NOT NULL,
    date_validation timestamp(6) with time zone,
    description text NOT NULL,
    devise character varying(3) NOT NULL,
    lieu_precis character varying(255),
    montant_confirme_justice numeric(38,2),
    montant_prejudice numeric(38,2) NOT NULL,
    motif_rejet character varying(255),
    statut_validation core.statut_validation NOT NULL,
    dossier_id uuid NOT NULL,
    type_infraction_id uuid NOT NULL,
    valide_par_id uuid,
    zone_geographique_id uuid
);


--
-- Name: implication; Type: TABLE; Schema: dossiers; Owner: -
--

CREATE TABLE dossiers.implication (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    autorite_competente character varying(255),
    date_creation timestamp(6) with time zone,
    date_debut date NOT NULL,
    date_fin date,
    entite_libelle_a_l_epoque character varying(255),
    fonction_occupee character varying(255),
    observations text,
    reference_affaire character varying(255),
    dossier_id uuid NOT NULL,
    entite_organisation_id uuid,
    personne_id uuid NOT NULL,
    role_implication_id uuid NOT NULL,
    statut_judiciaire_id uuid
);


--
-- Name: implication_fait; Type: TABLE; Schema: dossiers; Owner: -
--

CREATE TABLE dossiers.implication_fait (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    date_creation timestamp(6) with time zone NOT NULL,
    date_maj timestamp(6) with time zone NOT NULL,
    version bigint NOT NULL,
    commentaire text,
    date_statut date NOT NULL,
    fait_reproche_id uuid NOT NULL,
    implication_id uuid NOT NULL,
    statut_judiciaire_id uuid NOT NULL
);


--
-- Name: peine; Type: TABLE; Schema: dossiers; Owner: -
--

CREATE TABLE dossiers.peine (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    date_creation timestamp(6) with time zone,
    date_decision date,
    date_execution date,
    description text,
    duree character varying(255),
    montant_amende numeric(38,2),
    nature_sanction core.nature_sanction,
    type_peine core.type_peine NOT NULL,
    implication_fait_id uuid NOT NULL
);


--
-- Name: alias; Type: TABLE; Schema: personnes; Owner: -
--

CREATE TABLE personnes.alias (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    commentaire character varying(255),
    nom_alias character varying(255) NOT NULL,
    personne_id uuid NOT NULL
);


--
-- Name: personne; Type: TABLE; Schema: personnes; Owner: -
--

CREATE TABLE personnes.personne (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    date_creation timestamp(6) with time zone NOT NULL,
    date_maj timestamp(6) with time zone NOT NULL,
    version bigint NOT NULL,
    nom_affichage character varying(255) NOT NULL,
    photo_chemin_stockage character varying(255),
    photo_nom_original character varying(255),
    photo_nom_stockage character varying(255),
    photo_type_mime character varying(255),
    type_personne core.type_personne NOT NULL,
    cree_par_id uuid NOT NULL
);


--
-- Name: personne_morale; Type: TABLE; Schema: personnes; Owner: -
--

CREATE TABLE personnes.personne_morale (
    capital_social numeric(38,2),
    date_creation_entreprise date,
    denomination_sociale character varying(255) NOT NULL,
    email character varying(255),
    forme_juridique character varying(255) NOT NULL,
    ifu character varying(255),
    rccm character varying(255),
    secteur_activite character varying(255) NOT NULL,
    siege_social character varying(255) NOT NULL,
    sigle character varying(255),
    statut core.statut_personne_morale NOT NULL,
    telephone character varying(255),
    id uuid NOT NULL,
    representant_legal_id uuid
);


--
-- Name: personne_physique; Type: TABLE; Schema: personnes; Owner: -
--

CREATE TABLE personnes.personne_physique (
    adresse character varying(255),
    date_naissance date,
    grade_categorie character varying(255),
    lieu_naissance character varying(255),
    matricule_fonction_publique character varying(255),
    nationalite character varying(100),
    nom_conjoint character varying(255),
    nom_naissance character varying(255) NOT NULL,
    nom_usage character varying(255),
    prenoms character varying(255) NOT NULL,
    profession character varying(255),
    sexe core.sexe NOT NULL,
    situation_matrimoniale core.situation_matrimoniale,
    telephone character varying(255),
    id uuid NOT NULL,
    nationalite_id uuid
);


--
-- Name: piece_identite; Type: TABLE; Schema: personnes; Owner: -
--

CREATE TABLE personnes.piece_identite (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    date_delivrance date,
    date_expiration date,
    numero character varying(255) NOT NULL,
    type_piece core.type_piece_identite,
    personne_physique_id uuid NOT NULL,
    type_piece_id uuid
);


--
-- Name: categorie_infraction; Type: TABLE; Schema: referentiels; Owner: -
--

CREATE TABLE referentiels.categorie_infraction (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    description character varying(255),
    libelle character varying(255) NOT NULL
);


--
-- Name: entite_organisation; Type: TABLE; Schema: referentiels; Owner: -
--

CREATE TABLE referentiels.entite_organisation (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    libelle character varying(255) NOT NULL,
    niveau core.niveau_entite NOT NULL,
    parent_id uuid
);


--
-- Name: nationalite; Type: TABLE; Schema: referentiels; Owner: -
--

CREATE TABLE referentiels.nationalite (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    actif boolean NOT NULL,
    code_iso character varying(3),
    libelle character varying(150) NOT NULL
);


--
-- Name: role_implication; Type: TABLE; Schema: referentiels; Owner: -
--

CREATE TABLE referentiels.role_implication (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    actif boolean NOT NULL,
    libelle character varying(255) NOT NULL
);


--
-- Name: source_signalement; Type: TABLE; Schema: referentiels; Owner: -
--

CREATE TABLE referentiels.source_signalement (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    description character varying(255),
    libelle character varying(255) NOT NULL
);


--
-- Name: statut_judiciaire; Type: TABLE; Schema: referentiels; Owner: -
--

CREATE TABLE referentiels.statut_judiciaire (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    actif boolean NOT NULL,
    libelle character varying(255) NOT NULL
);


--
-- Name: type_document; Type: TABLE; Schema: referentiels; Owner: -
--

CREATE TABLE referentiels.type_document (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    actif boolean NOT NULL,
    libelle character varying(255) NOT NULL
);


--
-- Name: type_infraction; Type: TABLE; Schema: referentiels; Owner: -
--

CREATE TABLE referentiels.type_infraction (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    actif boolean NOT NULL,
    libelle character varying(255) NOT NULL,
    categorie_infraction_id uuid NOT NULL
);


--
-- Name: type_piece_identite; Type: TABLE; Schema: referentiels; Owner: -
--

CREATE TABLE referentiels.type_piece_identite (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    actif boolean NOT NULL,
    code character varying(30) NOT NULL,
    libelle character varying(150) NOT NULL
);


--
-- Name: zone_geographique; Type: TABLE; Schema: referentiels; Owner: -
--

CREATE TABLE referentiels.zone_geographique (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    code character varying(255),
    libelle character varying(255) NOT NULL,
    niveau core.niveau_zone NOT NULL,
    parent_id uuid
);


--
-- Name: role_habilitation; Type: TABLE; Schema: securite; Owner: -
--

CREATE TABLE securite.role_habilitation (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    acces_vue_globale_dossier boolean NOT NULL,
    code core.code_role NOT NULL,
    libelle character varying(255) NOT NULL
);


--
-- Name: utilisateur; Type: TABLE; Schema: securite; Owner: -
--

CREATE TABLE securite.utilisateur (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    actif boolean NOT NULL,
    date_creation timestamp(6) with time zone,
    date_maj timestamp(6) with time zone,
    email character varying(255) NOT NULL,
    keycloak_id character varying(255) NOT NULL,
    nom character varying(255) NOT NULL,
    prenom character varying(255) NOT NULL,
    telephone character varying(255)
);


--
-- Name: utilisateur_role; Type: TABLE; Schema: securite; Owner: -
--

CREATE TABLE securite.utilisateur_role (
    id uuid DEFAULT core.uuid_generate_v7() NOT NULL,
    date_attribution timestamp(6) with time zone,
    role_habilitation_id uuid NOT NULL,
    utilisateur_id uuid NOT NULL
);


--
-- Name: journal_audit journal_audit_pkey; Type: CONSTRAINT; Schema: audit; Owner: -
--

ALTER TABLE ONLY audit.journal_audit
    ADD CONSTRAINT journal_audit_pkey PRIMARY KEY (date_action, id);


--
-- Name: journal_consultation journal_consultation_pkey; Type: CONSTRAINT; Schema: audit; Owner: -
--

ALTER TABLE ONLY audit.journal_consultation
    ADD CONSTRAINT journal_consultation_pkey PRIMARY KEY (date_consultation, id);


--
-- Name: parametre_systeme parametre_systeme_pkey; Type: CONSTRAINT; Schema: core; Owner: -
--

ALTER TABLE ONLY core.parametre_systeme
    ADD CONSTRAINT parametre_systeme_pkey PRIMARY KEY (id);


--
-- Name: parametre_systeme ukr11m6la6ty04t7cqqvkp7odx5; Type: CONSTRAINT; Schema: core; Owner: -
--

ALTER TABLE ONLY core.parametre_systeme
    ADD CONSTRAINT ukr11m6la6ty04t7cqqvkp7odx5 UNIQUE (cle);


--
-- Name: document_implication document_implication_pkey; Type: CONSTRAINT; Schema: documents; Owner: -
--

ALTER TABLE ONLY documents.document_implication
    ADD CONSTRAINT document_implication_pkey PRIMARY KEY (document_id, implication_id);


--
-- Name: document document_pkey; Type: CONSTRAINT; Schema: documents; Owner: -
--

ALTER TABLE ONLY documents.document
    ADD CONSTRAINT document_pkey PRIMARY KEY (id);


--
-- Name: dossier dossier_pkey; Type: CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.dossier
    ADD CONSTRAINT dossier_pkey PRIMARY KEY (id);


--
-- Name: fait_reproche fait_reproche_pkey; Type: CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.fait_reproche
    ADD CONSTRAINT fait_reproche_pkey PRIMARY KEY (id);


--
-- Name: implication_fait implication_fait_pkey; Type: CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.implication_fait
    ADD CONSTRAINT implication_fait_pkey PRIMARY KEY (id);


--
-- Name: implication implication_pkey; Type: CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.implication
    ADD CONSTRAINT implication_pkey PRIMARY KEY (id);


--
-- Name: peine peine_pkey; Type: CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.peine
    ADD CONSTRAINT peine_pkey PRIMARY KEY (id);


--
-- Name: alias alias_pkey; Type: CONSTRAINT; Schema: personnes; Owner: -
--

ALTER TABLE ONLY personnes.alias
    ADD CONSTRAINT alias_pkey PRIMARY KEY (id);


--
-- Name: personne_morale personne_morale_pkey; Type: CONSTRAINT; Schema: personnes; Owner: -
--

ALTER TABLE ONLY personnes.personne_morale
    ADD CONSTRAINT personne_morale_pkey PRIMARY KEY (id);


--
-- Name: personne_physique personne_physique_pkey; Type: CONSTRAINT; Schema: personnes; Owner: -
--

ALTER TABLE ONLY personnes.personne_physique
    ADD CONSTRAINT personne_physique_pkey PRIMARY KEY (id);


--
-- Name: personne personne_pkey; Type: CONSTRAINT; Schema: personnes; Owner: -
--

ALTER TABLE ONLY personnes.personne
    ADD CONSTRAINT personne_pkey PRIMARY KEY (id);


--
-- Name: piece_identite piece_identite_pkey; Type: CONSTRAINT; Schema: personnes; Owner: -
--

ALTER TABLE ONLY personnes.piece_identite
    ADD CONSTRAINT piece_identite_pkey PRIMARY KEY (id);


--
-- Name: categorie_infraction categorie_infraction_pkey; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.categorie_infraction
    ADD CONSTRAINT categorie_infraction_pkey PRIMARY KEY (id);


--
-- Name: entite_organisation entite_organisation_pkey; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.entite_organisation
    ADD CONSTRAINT entite_organisation_pkey PRIMARY KEY (id);


--
-- Name: nationalite nationalite_pkey; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.nationalite
    ADD CONSTRAINT nationalite_pkey PRIMARY KEY (id);


--
-- Name: role_implication role_implication_pkey; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.role_implication
    ADD CONSTRAINT role_implication_pkey PRIMARY KEY (id);


--
-- Name: source_signalement source_signalement_pkey; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.source_signalement
    ADD CONSTRAINT source_signalement_pkey PRIMARY KEY (id);


--
-- Name: statut_judiciaire statut_judiciaire_pkey; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.statut_judiciaire
    ADD CONSTRAINT statut_judiciaire_pkey PRIMARY KEY (id);


--
-- Name: type_document type_document_pkey; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.type_document
    ADD CONSTRAINT type_document_pkey PRIMARY KEY (id);


--
-- Name: type_infraction type_infraction_pkey; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.type_infraction
    ADD CONSTRAINT type_infraction_pkey PRIMARY KEY (id);


--
-- Name: type_piece_identite type_piece_identite_pkey; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.type_piece_identite
    ADD CONSTRAINT type_piece_identite_pkey PRIMARY KEY (id);


--
-- Name: source_signalement uk1op9pnkdsi6w1xqhgad3qk2gj; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.source_signalement
    ADD CONSTRAINT uk1op9pnkdsi6w1xqhgad3qk2gj UNIQUE (libelle);


--
-- Name: type_piece_identite uk2od5e41dfshf2h7knn5bum0xv; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.type_piece_identite
    ADD CONSTRAINT uk2od5e41dfshf2h7knn5bum0xv UNIQUE (code);


--
-- Name: role_implication uk4gs5wwswtgtwd9ooe6vit6eyd; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.role_implication
    ADD CONSTRAINT uk4gs5wwswtgtwd9ooe6vit6eyd UNIQUE (libelle);


--
-- Name: statut_judiciaire uk68aiie41e2xl71qno40ubfyol; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.statut_judiciaire
    ADD CONSTRAINT uk68aiie41e2xl71qno40ubfyol UNIQUE (libelle);


--
-- Name: categorie_infraction ukd50dvrjnu9447htqq6hni3o6j; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.categorie_infraction
    ADD CONSTRAINT ukd50dvrjnu9447htqq6hni3o6j UNIQUE (libelle);


--
-- Name: type_document ukf3971833c1ffvp6dlp8h1j084; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.type_document
    ADD CONSTRAINT ukf3971833c1ffvp6dlp8h1j084 UNIQUE (libelle);


--
-- Name: nationalite ukfc8ap27xie0cl08gfulakpg35; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.nationalite
    ADD CONSTRAINT ukfc8ap27xie0cl08gfulakpg35 UNIQUE (libelle);


--
-- Name: type_infraction ukunyeeptby50eo8g3ov9el4t7; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.type_infraction
    ADD CONSTRAINT ukunyeeptby50eo8g3ov9el4t7 UNIQUE (libelle);


--
-- Name: zone_geographique zone_geographique_pkey; Type: CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.zone_geographique
    ADD CONSTRAINT zone_geographique_pkey PRIMARY KEY (id);


--
-- Name: role_habilitation role_habilitation_pkey; Type: CONSTRAINT; Schema: securite; Owner: -
--

ALTER TABLE ONLY securite.role_habilitation
    ADD CONSTRAINT role_habilitation_pkey PRIMARY KEY (id);


--
-- Name: role_habilitation uk38eipl5usc44j7u461ptsu61h; Type: CONSTRAINT; Schema: securite; Owner: -
--

ALTER TABLE ONLY securite.role_habilitation
    ADD CONSTRAINT uk38eipl5usc44j7u461ptsu61h UNIQUE (code);


--
-- Name: utilisateur ukguguhebxdlkdb60059i58tytd; Type: CONSTRAINT; Schema: securite; Owner: -
--

ALTER TABLE ONLY securite.utilisateur
    ADD CONSTRAINT ukguguhebxdlkdb60059i58tytd UNIQUE (keycloak_id);


--
-- Name: utilisateur ukrma38wvnqfaf66vvmi57c71lo; Type: CONSTRAINT; Schema: securite; Owner: -
--

ALTER TABLE ONLY securite.utilisateur
    ADD CONSTRAINT ukrma38wvnqfaf66vvmi57c71lo UNIQUE (email);


--
-- Name: utilisateur utilisateur_pkey; Type: CONSTRAINT; Schema: securite; Owner: -
--

ALTER TABLE ONLY securite.utilisateur
    ADD CONSTRAINT utilisateur_pkey PRIMARY KEY (id);


--
-- Name: utilisateur_role utilisateur_role_pkey; Type: CONSTRAINT; Schema: securite; Owner: -
--

ALTER TABLE ONLY securite.utilisateur_role
    ADD CONSTRAINT utilisateur_role_pkey PRIMARY KEY (id);


--
-- Name: journal_audit fk3p9vuad5i9jt8l0pf9yy6614m; Type: FK CONSTRAINT; Schema: audit; Owner: -
--

ALTER TABLE ONLY audit.journal_audit
    ADD CONSTRAINT fk3p9vuad5i9jt8l0pf9yy6614m FOREIGN KEY (utilisateur_id) REFERENCES securite.utilisateur(id);


--
-- Name: journal_consultation fkoro2hgu663wv6625hq5ra09dq; Type: FK CONSTRAINT; Schema: audit; Owner: -
--

ALTER TABLE ONLY audit.journal_consultation
    ADD CONSTRAINT fkoro2hgu663wv6625hq5ra09dq FOREIGN KEY (utilisateur_id) REFERENCES securite.utilisateur(id);


--
-- Name: document_implication fk5cy8g31knjmerloui8ccfetgc; Type: FK CONSTRAINT; Schema: documents; Owner: -
--

ALTER TABLE ONLY documents.document_implication
    ADD CONSTRAINT fk5cy8g31knjmerloui8ccfetgc FOREIGN KEY (implication_id) REFERENCES dossiers.implication(id);


--
-- Name: document fk9m3amtuv04yp105icox4r0prs; Type: FK CONSTRAINT; Schema: documents; Owner: -
--

ALTER TABLE ONLY documents.document
    ADD CONSTRAINT fk9m3amtuv04yp105icox4r0prs FOREIGN KEY (uploade_par_id) REFERENCES securite.utilisateur(id);


--
-- Name: document fke8bxll1qey8hrnj2i9t12s6n7; Type: FK CONSTRAINT; Schema: documents; Owner: -
--

ALTER TABLE ONLY documents.document
    ADD CONSTRAINT fke8bxll1qey8hrnj2i9t12s6n7 FOREIGN KEY (type_document_id) REFERENCES referentiels.type_document(id);


--
-- Name: document_implication fkholhvfxeyk324628peijtvx9u; Type: FK CONSTRAINT; Schema: documents; Owner: -
--

ALTER TABLE ONLY documents.document_implication
    ADD CONSTRAINT fkholhvfxeyk324628peijtvx9u FOREIGN KEY (document_id) REFERENCES documents.document(id);


--
-- Name: document fkps7b7hv2uwwm4bxe1gfvui6st; Type: FK CONSTRAINT; Schema: documents; Owner: -
--

ALTER TABLE ONLY documents.document
    ADD CONSTRAINT fkps7b7hv2uwwm4bxe1gfvui6st FOREIGN KEY (dossier_id) REFERENCES dossiers.dossier(id);


--
-- Name: fait_reproche fk2vfyw5eli53uvkovhoakgoi9g; Type: FK CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.fait_reproche
    ADD CONSTRAINT fk2vfyw5eli53uvkovhoakgoi9g FOREIGN KEY (type_infraction_id) REFERENCES referentiels.type_infraction(id);


--
-- Name: implication fk3ohkxei02mhh1xc8ivegpe2nk; Type: FK CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.implication
    ADD CONSTRAINT fk3ohkxei02mhh1xc8ivegpe2nk FOREIGN KEY (role_implication_id) REFERENCES referentiels.role_implication(id);


--
-- Name: fait_reproche fk4r5piymbx00rijelamcwwdxpr; Type: FK CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.fait_reproche
    ADD CONSTRAINT fk4r5piymbx00rijelamcwwdxpr FOREIGN KEY (dossier_id) REFERENCES dossiers.dossier(id);


--
-- Name: peine fk67w3fabhrxr8f7xi7e903ocwg; Type: FK CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.peine
    ADD CONSTRAINT fk67w3fabhrxr8f7xi7e903ocwg FOREIGN KEY (implication_fait_id) REFERENCES dossiers.implication_fait(id);


--
-- Name: implication fk6sqd538vaaevibirva0pc3pnm; Type: FK CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.implication
    ADD CONSTRAINT fk6sqd538vaaevibirva0pc3pnm FOREIGN KEY (dossier_id) REFERENCES dossiers.dossier(id);


--
-- Name: implication_fait fkb7ku6tbm75yidn55hxavi6h4x; Type: FK CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.implication_fait
    ADD CONSTRAINT fkb7ku6tbm75yidn55hxavi6h4x FOREIGN KEY (implication_id) REFERENCES dossiers.implication(id);


--
-- Name: fait_reproche fke2qhbdle8y3ypueutn492a83s; Type: FK CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.fait_reproche
    ADD CONSTRAINT fke2qhbdle8y3ypueutn492a83s FOREIGN KEY (valide_par_id) REFERENCES securite.utilisateur(id);


--
-- Name: implication fkejxe6lt8tlwswji0vx0p4ihiq; Type: FK CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.implication
    ADD CONSTRAINT fkejxe6lt8tlwswji0vx0p4ihiq FOREIGN KEY (personne_id) REFERENCES personnes.personne(id);


--
-- Name: implication fkgfwwn1j6wmjyuuaw40rdj21d; Type: FK CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.implication
    ADD CONSTRAINT fkgfwwn1j6wmjyuuaw40rdj21d FOREIGN KEY (entite_organisation_id) REFERENCES referentiels.entite_organisation(id);


--
-- Name: implication_fait fkih8h2ru1mblyqc1m6xb2jjeil; Type: FK CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.implication_fait
    ADD CONSTRAINT fkih8h2ru1mblyqc1m6xb2jjeil FOREIGN KEY (statut_judiciaire_id) REFERENCES referentiels.statut_judiciaire(id);


--
-- Name: dossier fkix2nti0y4tejxs1qr6m3ukh3e; Type: FK CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.dossier
    ADD CONSTRAINT fkix2nti0y4tejxs1qr6m3ukh3e FOREIGN KEY (source_signalement_id) REFERENCES referentiels.source_signalement(id);


--
-- Name: fait_reproche fkojg4ebt1sggk0sy2wmjkvh8bo; Type: FK CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.fait_reproche
    ADD CONSTRAINT fkojg4ebt1sggk0sy2wmjkvh8bo FOREIGN KEY (zone_geographique_id) REFERENCES referentiels.zone_geographique(id);


--
-- Name: implication_fait fkpn03jcb8dv0hglxevuicxl0f1; Type: FK CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.implication_fait
    ADD CONSTRAINT fkpn03jcb8dv0hglxevuicxl0f1 FOREIGN KEY (fait_reproche_id) REFERENCES dossiers.fait_reproche(id);


--
-- Name: implication fkqpe3l456ryir16fj5qav2urny; Type: FK CONSTRAINT; Schema: dossiers; Owner: -
--

ALTER TABLE ONLY dossiers.implication
    ADD CONSTRAINT fkqpe3l456ryir16fj5qav2urny FOREIGN KEY (statut_judiciaire_id) REFERENCES referentiels.statut_judiciaire(id);


--
-- Name: alias fk1h5da5j5x1sciqjbdm2o93qnd; Type: FK CONSTRAINT; Schema: personnes; Owner: -
--

ALTER TABLE ONLY personnes.alias
    ADD CONSTRAINT fk1h5da5j5x1sciqjbdm2o93qnd FOREIGN KEY (personne_id) REFERENCES personnes.personne(id);


--
-- Name: personne fk3xi6dry7gvwheie9bf4klqmx8; Type: FK CONSTRAINT; Schema: personnes; Owner: -
--

ALTER TABLE ONLY personnes.personne
    ADD CONSTRAINT fk3xi6dry7gvwheie9bf4klqmx8 FOREIGN KEY (cree_par_id) REFERENCES securite.utilisateur(id);


--
-- Name: piece_identite fk4hue4w3o7d7118i9xw9y25tf4; Type: FK CONSTRAINT; Schema: personnes; Owner: -
--

ALTER TABLE ONLY personnes.piece_identite
    ADD CONSTRAINT fk4hue4w3o7d7118i9xw9y25tf4 FOREIGN KEY (type_piece_id) REFERENCES referentiels.type_piece_identite(id);


--
-- Name: personne_morale fkd0argsea72cd00cwspg44xv7c; Type: FK CONSTRAINT; Schema: personnes; Owner: -
--

ALTER TABLE ONLY personnes.personne_morale
    ADD CONSTRAINT fkd0argsea72cd00cwspg44xv7c FOREIGN KEY (id) REFERENCES personnes.personne(id);


--
-- Name: personne_physique fkdntqhp735wc4j2lt9wd0es7gc; Type: FK CONSTRAINT; Schema: personnes; Owner: -
--

ALTER TABLE ONLY personnes.personne_physique
    ADD CONSTRAINT fkdntqhp735wc4j2lt9wd0es7gc FOREIGN KEY (id) REFERENCES personnes.personne(id);


--
-- Name: piece_identite fkfi15tyffma4j78n4dwi4w1hhj; Type: FK CONSTRAINT; Schema: personnes; Owner: -
--

ALTER TABLE ONLY personnes.piece_identite
    ADD CONSTRAINT fkfi15tyffma4j78n4dwi4w1hhj FOREIGN KEY (personne_physique_id) REFERENCES personnes.personne_physique(id);


--
-- Name: personne_physique fkjknhnqi5ddk0qi6tlhemq133y; Type: FK CONSTRAINT; Schema: personnes; Owner: -
--

ALTER TABLE ONLY personnes.personne_physique
    ADD CONSTRAINT fkjknhnqi5ddk0qi6tlhemq133y FOREIGN KEY (nationalite_id) REFERENCES referentiels.nationalite(id);


--
-- Name: personne_morale fkrtn9w3ir4iwibmqneg7l2mh52; Type: FK CONSTRAINT; Schema: personnes; Owner: -
--

ALTER TABLE ONLY personnes.personne_morale
    ADD CONSTRAINT fkrtn9w3ir4iwibmqneg7l2mh52 FOREIGN KEY (representant_legal_id) REFERENCES personnes.personne_physique(id);


--
-- Name: zone_geographique fk92kjelebqcvuitdslbeqakiwm; Type: FK CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.zone_geographique
    ADD CONSTRAINT fk92kjelebqcvuitdslbeqakiwm FOREIGN KEY (parent_id) REFERENCES referentiels.zone_geographique(id);


--
-- Name: entite_organisation fkiqmcgqocqcerlga5xc1a9tjb5; Type: FK CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.entite_organisation
    ADD CONSTRAINT fkiqmcgqocqcerlga5xc1a9tjb5 FOREIGN KEY (parent_id) REFERENCES referentiels.entite_organisation(id);


--
-- Name: type_infraction fktlmit28vkblkyq2t9c9tr0sbc; Type: FK CONSTRAINT; Schema: referentiels; Owner: -
--

ALTER TABLE ONLY referentiels.type_infraction
    ADD CONSTRAINT fktlmit28vkblkyq2t9c9tr0sbc FOREIGN KEY (categorie_infraction_id) REFERENCES referentiels.categorie_infraction(id);


--
-- Name: utilisateur_role fk6kifvrsfkpqn502r5ipjl5pvu; Type: FK CONSTRAINT; Schema: securite; Owner: -
--

ALTER TABLE ONLY securite.utilisateur_role
    ADD CONSTRAINT fk6kifvrsfkpqn502r5ipjl5pvu FOREIGN KEY (utilisateur_id) REFERENCES securite.utilisateur(id);


--
-- Name: utilisateur_role fkbldmapgpq00osuhpv90tdh6fy; Type: FK CONSTRAINT; Schema: securite; Owner: -
--

ALTER TABLE ONLY securite.utilisateur_role
    ADD CONSTRAINT fkbldmapgpq00osuhpv90tdh6fy FOREIGN KEY (role_habilitation_id) REFERENCES securite.role_habilitation(id);


--
--


