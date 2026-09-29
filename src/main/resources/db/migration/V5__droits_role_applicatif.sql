-- V5 : moindre privilege pour le role d'execution de l'application.
-- Les migrations tournent avec un role proprietaire (DDL) ; l'application, elle, se connecte avec
-- logintegrite_app : lecture/ecriture des donnees, aucun droit de modifier le schema ni de
-- desactiver les triggers d'audit. Sans ce role (developpement), la migration est sans effet.

DO $$
DECLARE
    s text;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'logintegrite_app') THEN
        RAISE NOTICE 'Role logintegrite_app absent : droits non appliques (mode developpement)';
        RETURN;
    END IF;

    FOREACH s IN ARRAY ARRAY['core','audit','documents','dossiers','personnes','referentiels','securite'] LOOP
        EXECUTE format('GRANT USAGE ON SCHEMA %I TO logintegrite_app', s);
        EXECUTE format('GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA %I TO logintegrite_app', s);
        EXECUTE format('GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA %I TO logintegrite_app', s);
        EXECUTE format('GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA %I TO logintegrite_app', s);
        -- Objets crees par les futures migrations
        EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA %I GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO logintegrite_app', s);
        EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA %I GRANT USAGE, SELECT ON SEQUENCES TO logintegrite_app', s);
        EXECUTE format('ALTER DEFAULT PRIVILEGES IN SCHEMA %I GRANT EXECUTE ON FUNCTIONS TO logintegrite_app', s);
    END LOOP;

    -- Defense en profondeur : en plus des triggers d'immuabilite, le role applicatif n'a meme pas
    -- le droit SQL de modifier ou supprimer le journal d'audit, les consultations et les documents.
    REVOKE UPDATE, DELETE, TRUNCATE ON audit.journal_audit, audit.journal_consultation, documents.document
        FROM logintegrite_app;

    -- L'historique des migrations n'est pas du ressort de l'application.
    REVOKE ALL ON core.flyway_schema_history FROM logintegrite_app;
END
$$;
