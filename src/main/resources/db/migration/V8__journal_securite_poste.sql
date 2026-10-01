-- V8 : journal des evenements de securite releves sur le poste de l'utilisateur
-- (tentative de copie, d'impression, menu contextuel, perte de focus...).
-- Meme garantie que le journal d'audit : ajout seulement, jamais de modification ni de suppression.

CREATE TABLE audit.journal_securite (
    id          uuid PRIMARY KEY DEFAULT core.uuid_generate_v7(),
    date_evenement timestamptz NOT NULL DEFAULT now(),
    utilisateur_id uuid NOT NULL REFERENCES securite.utilisateur(id),
    type_evenement varchar(40) NOT NULL,
    page         varchar(255),
    detail       varchar(500),
    adresse_ip   varchar(64),
    user_agent   varchar(400)
);

CREATE INDEX idx_journal_securite_date ON audit.journal_securite (date_evenement DESC);
CREATE INDEX idx_journal_securite_utilisateur ON audit.journal_securite (utilisateur_id, date_evenement DESC);

CREATE TRIGGER trg_journal_securite_immuable
    BEFORE UPDATE OR DELETE ON audit.journal_securite
    FOR EACH ROW EXECUTE FUNCTION audit.interdire_modification();
CREATE TRIGGER trg_journal_securite_truncate
    BEFORE TRUNCATE ON audit.journal_securite
    FOR EACH STATEMENT EXECUTE FUNCTION audit.interdire_modification();

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'logintegrite_app') THEN
        REVOKE UPDATE, DELETE, TRUNCATE ON audit.journal_securite FROM logintegrite_app;
    END IF;
END
$$;
