-- V13 : archive des etats trimestriels des personnes fichees remis a la hierarchie.
-- Un etat n'est jamais supprime : une regeneration marque l'ancien "remplace" et le conserve.

CREATE TABLE audit.etat_trimestriel (
    id               uuid PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    annee            smallint NOT NULL,
    trimestre        smallint NOT NULL CHECK (trimestre BETWEEN 1 AND 4),
    date_arret       date NOT NULL,
    date_generation  timestamptz NOT NULL DEFAULT now(),
    genere_par       varchar(150) NOT NULL,
    nb_personnes     integer NOT NULL,
    personne_ids     uuid[] NOT NULL,
    pdf              bytea NOT NULL,
    excel            bytea NOT NULL,
    sha256_pdf       char(64) NOT NULL,
    sha256_excel     char(64) NOT NULL,
    remplace         boolean NOT NULL DEFAULT false
);

-- un seul etat actif par trimestre
CREATE UNIQUE INDEX uq_etat_trimestriel_actif ON audit.etat_trimestriel (annee, trimestre) WHERE NOT remplace;

CREATE TRIGGER trg_etat_trimestriel_pas_de_suppression
    BEFORE DELETE ON audit.etat_trimestriel
    FOR EACH ROW EXECUTE FUNCTION audit.interdire_modification();
CREATE TRIGGER trg_etat_trimestriel_pas_de_truncate
    BEFORE TRUNCATE ON audit.etat_trimestriel
    FOR EACH STATEMENT EXECUTE FUNCTION audit.interdire_modification();

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'logintegrite_app') THEN
        REVOKE DELETE, TRUNCATE ON audit.etat_trimestriel FROM logintegrite_app;
    END IF;
END
$$;
