-- V4 : colonnes alimentees par la base.
-- Les entites les declarent insertable=false (date_creation, date_upload, date_attribution...) :
-- le schema doit donc fournir leur valeur. Sans ces DEFAULT, toute creation echoue en NOT NULL.

ALTER TABLE documents.document             ALTER COLUMN date_upload      SET DEFAULT now();
ALTER TABLE documents.document_implication ALTER COLUMN date_creation    SET DEFAULT now();
ALTER TABLE dossiers.dossier               ALTER COLUMN date_creation    SET DEFAULT now();
ALTER TABLE dossiers.dossier               ALTER COLUMN date_maj         SET DEFAULT now();
ALTER TABLE dossiers.fait_reproche         ALTER COLUMN date_creation    SET DEFAULT now();
ALTER TABLE dossiers.fait_reproche         ALTER COLUMN date_maj         SET DEFAULT now();
ALTER TABLE dossiers.implication           ALTER COLUMN date_creation    SET DEFAULT now();
ALTER TABLE dossiers.implication_fait      ALTER COLUMN date_creation    SET DEFAULT now();
ALTER TABLE dossiers.implication_fait      ALTER COLUMN date_maj         SET DEFAULT now();
ALTER TABLE dossiers.peine                 ALTER COLUMN date_creation    SET DEFAULT now();
ALTER TABLE personnes.personne             ALTER COLUMN date_creation    SET DEFAULT now();
ALTER TABLE personnes.personne             ALTER COLUMN date_maj         SET DEFAULT now();
ALTER TABLE securite.utilisateur           ALTER COLUMN date_creation    SET DEFAULT now();
ALTER TABLE securite.utilisateur           ALTER COLUMN date_maj         SET DEFAULT now();
ALTER TABLE securite.utilisateur_role      ALTER COLUMN date_attribution SET DEFAULT now();

-- date_maj est maintenue par la base a chaque UPDATE (l'application ne la renseigne pas).
CREATE FUNCTION core.touch_date_maj() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    NEW.date_maj := now();
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_dossier_date_maj          BEFORE UPDATE ON dossiers.dossier
    FOR EACH ROW EXECUTE FUNCTION core.touch_date_maj();
CREATE TRIGGER trg_fait_reproche_date_maj    BEFORE UPDATE ON dossiers.fait_reproche
    FOR EACH ROW EXECUTE FUNCTION core.touch_date_maj();
CREATE TRIGGER trg_implication_fait_date_maj BEFORE UPDATE ON dossiers.implication_fait
    FOR EACH ROW EXECUTE FUNCTION core.touch_date_maj();
CREATE TRIGGER trg_personne_date_maj         BEFORE UPDATE ON personnes.personne
    FOR EACH ROW EXECUTE FUNCTION core.touch_date_maj();
CREATE TRIGGER trg_utilisateur_date_maj      BEFORE UPDATE ON securite.utilisateur
    FOR EACH ROW EXECUTE FUNCTION core.touch_date_maj();
