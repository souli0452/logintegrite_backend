-- V9 : demandes d'export d'un dossier complet par un compte de consultation.
-- Un consultant consulte a l'ecran ; l'export d'un dossier se fait SUR DEMANDE motivee, accordee ou refusee par un administrateur.
-- Les demandes ne sont jamais supprimees (preuve de qui a demande quoi, pourquoi, et la decision prise).

CREATE TABLE audit.demande_export (
    id               uuid PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    date_demande     timestamptz NOT NULL DEFAULT now(),
    demandeur_id     uuid NOT NULL REFERENCES securite.utilisateur(id),
    personne_id      uuid NOT NULL REFERENCES personnes.personne(id),
    motif            varchar(1000) NOT NULL,
    statut           varchar(20) NOT NULL DEFAULT 'EN_ATTENTE'
                     CHECK (statut IN ('EN_ATTENTE', 'ACCORDEE', 'REFUSEE')),
    traite_par_id    uuid REFERENCES securite.utilisateur(id),
    date_traitement  timestamptz,
    commentaire      varchar(1000)
);

CREATE INDEX idx_demande_export_statut ON audit.demande_export (statut, date_demande DESC);
CREATE INDEX idx_demande_export_demandeur ON audit.demande_export (demandeur_id, date_demande DESC);
-- une seule demande en attente par couple demandeur / personne
CREATE UNIQUE INDEX uq_demande_export_attente ON audit.demande_export (demandeur_id, personne_id) WHERE statut = 'EN_ATTENTE';

CREATE TRIGGER trg_demande_export_pas_de_suppression
    BEFORE DELETE ON audit.demande_export
    FOR EACH ROW EXECUTE FUNCTION audit.interdire_modification();
CREATE TRIGGER trg_demande_export_pas_de_truncate
    BEFORE TRUNCATE ON audit.demande_export
    FOR EACH STATEMENT EXECUTE FUNCTION audit.interdire_modification();

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'logintegrite_app') THEN
        REVOKE DELETE, TRUNCATE ON audit.demande_export FROM logintegrite_app;
    END IF;
END
$$;
