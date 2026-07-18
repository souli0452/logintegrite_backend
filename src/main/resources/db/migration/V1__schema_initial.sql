--
-- PostgreSQL database dump
--

\restrict 9AbwRgXgHJMurf3rFMR1mOuedeRUQt4xgrYa5SJdRaZr3dowbLfOvVZazeu40Ne

-- Dumped from database version 15.18
-- Dumped by pg_dump version 15.18

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: entite_organisation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.entite_organisation (
    actif boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    code character varying(20) NOT NULL,
    type character varying(50),
    created_by_id character varying(100),
    current_user_first_name character varying(100),
    current_user_last_name character varying(100),
    updated_by_id character varying(100),
    current_user_email character varying(150),
    nom character varying(200) NOT NULL
);


--
-- Name: fiche_mise_en_cause; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fiche_mise_en_cause (
    date_naissance date,
    deleted boolean NOT NULL,
    nb_soumissions integer NOT NULL,
    version integer,
    created_at timestamp(6) without time zone NOT NULL,
    date_validation timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    entite_id uuid,
    id uuid NOT NULL,
    region_id uuid,
    statut_fiche character varying(30) NOT NULL,
    type_fiche character varying(31) NOT NULL,
    ifu character varying(50),
    matricule character varying(50),
    sigle character varying(50),
    statut_judiciaire character varying(50) NOT NULL,
    type_structure character varying(50),
    created_by_id character varying(100),
    current_user_first_name character varying(100),
    current_user_last_name character varying(100),
    lieu_naissance character varying(100),
    nationalite character varying(100),
    nom character varying(100),
    updated_by_id character varying(100),
    validateur_id character varying(100),
    current_user_email character varying(150),
    fonction character varying(200),
    fonction_responsable character varying(200),
    nom_responsable character varying(200),
    prenoms character varying(200),
    raison_sociale character varying(300),
    photo_url character varying(500),
    motif_rejet text,
    CONSTRAINT fiche_mise_en_cause_type_fiche_check CHECK (((type_fiche)::text = ANY ((ARRAY['PERSONNE_MORALE'::character varying, 'PERSONNE_PHYSIQUE'::character varying])::text[])))
);


--
-- Name: historique_statut; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.historique_statut (
    date_jugement date,
    montant_amende numeric(15,2),
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    fiche_id uuid NOT NULL,
    id uuid NOT NULL,
    ancien_statut character varying(50),
    nouveau_statut character varying(50) NOT NULL,
    created_by_id character varying(100),
    current_user_first_name character varying(100),
    current_user_last_name character varying(100),
    duree_peine character varying(100),
    updated_by_id character varying(100),
    current_user_email character varying(150),
    juridiction character varying(200),
    type_peine character varying(200),
    motif text,
    motif_relaxe text,
    infraction_id uuid
);


--
-- Name: infraction; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.infraction (
    date_faits date NOT NULL,
    montant numeric(15,2),
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    devise character varying(10),
    fiche_id uuid NOT NULL,
    id uuid NOT NULL,
    type_infraction_id uuid,
    nature character varying(30) NOT NULL,
    created_by_id character varying(100),
    current_user_first_name character varying(100),
    current_user_last_name character varying(100),
    updated_by_id character varying(100),
    current_user_email character varying(150),
    lieu_faits character varying(200),
    description text,
    sources text
);


--
-- Name: journal_audit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.journal_audit (
    created_at timestamp(6) without time zone NOT NULL,
    horodatage timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    statut character varying(20),
    ressource_id character varying(36),
    adresse_ip character varying(45),
    action character varying(50) NOT NULL,
    role character varying(50),
    created_by_id character varying(100),
    current_user_first_name character varying(100),
    current_user_last_name character varying(100),
    ressource_type character varying(100),
    updated_by_id character varying(100),
    utilisateur_id character varying(100),
    current_user_email character varying(150),
    username character varying(200),
    description text
);


--
-- Name: notification; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notification (
    lue boolean,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    type character varying(50),
    created_by_id character varying(100),
    current_user_first_name character varying(100),
    current_user_last_name character varying(100),
    destinataire_id character varying(100) NOT NULL,
    updated_by_id character varying(100),
    current_user_email character varying(150),
    contenu text,
    ressource_id character varying(100),
    ressource_type character varying(50)
);


--
-- Name: piece_jointe; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.piece_jointe (
    created_at timestamp(6) without time zone NOT NULL,
    taille_octets bigint,
    updated_at timestamp(6) without time zone NOT NULL,
    fiche_id uuid NOT NULL,
    id uuid NOT NULL,
    type_fichier character varying(50),
    created_by_id character varying(100),
    current_user_first_name character varying(100),
    current_user_last_name character varying(100),
    updated_by_id character varying(100),
    current_user_email character varying(150),
    url_stockage character varying(500) NOT NULL,
    nom_fichier character varying(255) NOT NULL,
    infraction_id uuid
);


--
-- Name: recherche_sauvegardee; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.recherche_sauvegardee (
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    created_by_id character varying(100),
    current_user_first_name character varying(100),
    current_user_last_name character varying(100),
    updated_by_id character varying(100),
    current_user_email character varying(150),
    nom character varying(200) NOT NULL,
    criteres text NOT NULL
);


--
-- Name: region; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.region (
    actif boolean NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    code character varying(10) NOT NULL,
    id uuid NOT NULL,
    created_by_id character varying(100),
    current_user_first_name character varying(100),
    current_user_last_name character varying(100),
    nom character varying(100) NOT NULL,
    updated_by_id character varying(100),
    current_user_email character varying(150)
);


--
-- Name: sauvegardes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.sauvegardes (
    created_at timestamp(6) without time zone NOT NULL,
    date_debut timestamp(6) without time zone NOT NULL,
    date_fin timestamp(6) without time zone,
    updated_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    created_by_id character varying(100),
    current_user_first_name character varying(100),
    current_user_last_name character varying(100),
    updated_by_id character varying(100),
    current_user_email character varying(150),
    nom_fichier character varying(255) NOT NULL,
    statut character varying(255) NOT NULL,
    type character varying(255) NOT NULL
);


--
-- Name: statut_judiciaire_referentiel; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.statut_judiciaire_referentiel (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone NOT NULL,
    created_by_id character varying(100),
    current_user_first_name character varying(100),
    current_user_last_name character varying(100),
    current_user_email character varying(150),
    updated_at timestamp(6) without time zone NOT NULL,
    updated_by_id character varying(100),
    actif boolean NOT NULL,
    code character varying(30) NOT NULL,
    description text,
    libelle character varying(150) NOT NULL
);


--
-- Name: type_infraction; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.type_infraction (
    created_at timestamp(6) without time zone NOT NULL,
    updated_at timestamp(6) without time zone NOT NULL,
    id uuid NOT NULL,
    code character varying(20) NOT NULL,
    created_by_id character varying(100),
    current_user_first_name character varying(100),
    current_user_last_name character varying(100),
    libelle character varying(100) NOT NULL,
    updated_by_id character varying(100),
    current_user_email character varying(150),
    description text,
    actif boolean DEFAULT true NOT NULL
);


--
-- Name: entite_organisation entite_organisation_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.entite_organisation
    ADD CONSTRAINT entite_organisation_code_key UNIQUE (code);


--
-- Name: entite_organisation entite_organisation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.entite_organisation
    ADD CONSTRAINT entite_organisation_pkey PRIMARY KEY (id);


--
-- Name: fiche_mise_en_cause fiche_mise_en_cause_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiche_mise_en_cause
    ADD CONSTRAINT fiche_mise_en_cause_pkey PRIMARY KEY (id);


--
-- Name: historique_statut historique_statut_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historique_statut
    ADD CONSTRAINT historique_statut_pkey PRIMARY KEY (id);


--
-- Name: infraction infraction_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.infraction
    ADD CONSTRAINT infraction_pkey PRIMARY KEY (id);


--
-- Name: journal_audit journal_audit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.journal_audit
    ADD CONSTRAINT journal_audit_pkey PRIMARY KEY (id);


--
-- Name: notification notification_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notification
    ADD CONSTRAINT notification_pkey PRIMARY KEY (id);


--
-- Name: piece_jointe piece_jointe_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.piece_jointe
    ADD CONSTRAINT piece_jointe_pkey PRIMARY KEY (id);


--
-- Name: recherche_sauvegardee recherche_sauvegardee_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.recherche_sauvegardee
    ADD CONSTRAINT recherche_sauvegardee_pkey PRIMARY KEY (id);


--
-- Name: region region_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.region
    ADD CONSTRAINT region_code_key UNIQUE (code);


--
-- Name: region region_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.region
    ADD CONSTRAINT region_pkey PRIMARY KEY (id);


--
-- Name: sauvegardes sauvegardes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.sauvegardes
    ADD CONSTRAINT sauvegardes_pkey PRIMARY KEY (id);


--
-- Name: statut_judiciaire_referentiel statut_judiciaire_referentiel_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.statut_judiciaire_referentiel
    ADD CONSTRAINT statut_judiciaire_referentiel_pkey PRIMARY KEY (id);


--
-- Name: type_infraction type_infraction_code_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.type_infraction
    ADD CONSTRAINT type_infraction_code_key UNIQUE (code);


--
-- Name: type_infraction type_infraction_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.type_infraction
    ADD CONSTRAINT type_infraction_pkey PRIMARY KEY (id);


--
-- Name: statut_judiciaire_referentiel ukq7gg3qx2hgagddbvkcamcwohd; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.statut_judiciaire_referentiel
    ADD CONSTRAINT ukq7gg3qx2hgagddbvkcamcwohd UNIQUE (code);


--
-- Name: uk_fiche_ifu_active; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_fiche_ifu_active ON public.fiche_mise_en_cause USING btree (ifu) WHERE (((statut_fiche)::text = 'ACTIVE'::text) AND ((type_fiche)::text = 'PERSONNE_MORALE'::text) AND (ifu IS NOT NULL) AND (deleted = false));


--
-- Name: uk_fiche_matricule_active; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uk_fiche_matricule_active ON public.fiche_mise_en_cause USING btree (matricule) WHERE (((statut_fiche)::text = 'ACTIVE'::text) AND ((type_fiche)::text = 'PERSONNE_PHYSIQUE'::text) AND (matricule IS NOT NULL) AND (deleted = false));


--
-- Name: piece_jointe fk63yx4egmujnhecfheid0cr4su; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.piece_jointe
    ADD CONSTRAINT fk63yx4egmujnhecfheid0cr4su FOREIGN KEY (infraction_id) REFERENCES public.infraction(id);


--
-- Name: fiche_mise_en_cause fk7rlv01nloyaurj4gdj8vjmjl1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiche_mise_en_cause
    ADD CONSTRAINT fk7rlv01nloyaurj4gdj8vjmjl1 FOREIGN KEY (region_id) REFERENCES public.region(id);


--
-- Name: infraction fk84puer0ffrw4l93a8f5q2djsx; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.infraction
    ADD CONSTRAINT fk84puer0ffrw4l93a8f5q2djsx FOREIGN KEY (fiche_id) REFERENCES public.fiche_mise_en_cause(id);


--
-- Name: historique_statut fk9omp3e42m344b3hwsmnstlb84; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historique_statut
    ADD CONSTRAINT fk9omp3e42m344b3hwsmnstlb84 FOREIGN KEY (fiche_id) REFERENCES public.fiche_mise_en_cause(id);


--
-- Name: fiche_mise_en_cause fkc4h5ehginaq9rvwpajefg127q; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fiche_mise_en_cause
    ADD CONSTRAINT fkc4h5ehginaq9rvwpajefg127q FOREIGN KEY (entite_id) REFERENCES public.entite_organisation(id);


--
-- Name: infraction fkelvicxcahn41b43awf69uxyqi; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.infraction
    ADD CONSTRAINT fkelvicxcahn41b43awf69uxyqi FOREIGN KEY (type_infraction_id) REFERENCES public.type_infraction(id);


--
-- Name: piece_jointe fkohkw04e85au3pla91ychvu7jj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.piece_jointe
    ADD CONSTRAINT fkohkw04e85au3pla91ychvu7jj FOREIGN KEY (fiche_id) REFERENCES public.fiche_mise_en_cause(id);


--
-- Name: historique_statut fkpmlpok0wydcr66g9s6h6hivbj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.historique_statut
    ADD CONSTRAINT fkpmlpok0wydcr66g9s6h6hivbj FOREIGN KEY (infraction_id) REFERENCES public.infraction(id);


--
-- PostgreSQL database dump complete
--

\unrestrict 9AbwRgXgHJMurf3rFMR1mOuedeRUQt4xgrYa5SJdRaZr3dowbLfOvVZazeu40Ne

