-- V7 : numerotation attribuee par le SERVEUR (base de donnees), sans doublon ni trou.
--   PERS-2026-00001 (personne physique), ORG-2026-00001 (personne morale), DOSS-2026-00001 (dossier)
--
-- Pourquoi : l'ancien numero de dossier reprenait les 8 premiers caracteres de l'identifiant v7, c'est-a-dire
-- l'horodatage a ~65 secondes pres : deux dossiers crees dans la meme minute avaient le meme numero. Le client
-- pouvait aussi imposer le sien, et rien n'exigeait l'unicite. Les personnes n'avaient aucun numero stocke.
--
-- Principe : un compteur par (type, annee) incremente dans la MEME transaction que la creation, verrouille par
-- la ligne du compteur : deux creations simultanees ne peuvent pas obtenir le meme numero, et une creation
-- annulee ne laisse pas de trou. L'attribution se fait par declencheur : le client ne peut plus imposer de numero.

-- 1. Compteur --------------------------------------------------------------------------------------------
CREATE TABLE core.compteur_numero (
    code    character varying(10) NOT NULL,
    annee   integer               NOT NULL,
    dernier integer               NOT NULL DEFAULT 0 CHECK (dernier >= 0),
    PRIMARY KEY (code, annee)
);

CREATE FUNCTION core.prochain_numero(p_code text) RETURNS text
    LANGUAGE plpgsql SECURITY DEFINER SET search_path = core, pg_temp
AS $$
DECLARE
    v_annee integer := EXTRACT(YEAR FROM (now() AT TIME ZONE 'UTC'))::integer;
    v_num   integer;
BEGIN
    INSERT INTO core.compteur_numero AS c (code, annee, dernier)
    VALUES (p_code, v_annee, 1)
    ON CONFLICT (code, annee) DO UPDATE SET dernier = c.dernier + 1
    RETURNING c.dernier INTO v_num;
    RETURN p_code || '-' || v_annee || '-' || lpad(v_num::text, 5, '0');
END
$$;

-- 2. Personnes -------------------------------------------------------------------------------------------
ALTER TABLE personnes.personne ADD COLUMN numero_personne character varying(32);

-- Attribution aux personnes existantes, dans l'ordre de creation (le declencheur de date_maj est suspendu :
-- cette numerotation ne doit pas modifier la date de mise a jour des fiches).
ALTER TABLE personnes.personne DISABLE TRIGGER trg_personne_date_maj;
WITH ordre AS (
    SELECT id,
           CASE type_personne WHEN 'PHYSIQUE' THEN 'PERS' ELSE 'ORG' END                 AS code,
           EXTRACT(YEAR FROM (date_creation AT TIME ZONE 'UTC'))::integer                 AS annee,
           row_number() OVER (
               PARTITION BY CASE type_personne WHEN 'PHYSIQUE' THEN 'PERS' ELSE 'ORG' END,
                            EXTRACT(YEAR FROM (date_creation AT TIME ZONE 'UTC'))::integer
               ORDER BY date_creation, id)                                                AS rang
    FROM personnes.personne
)
UPDATE personnes.personne p
   SET numero_personne = o.code || '-' || o.annee || '-' || lpad(o.rang::text, 5, '0')
  FROM ordre o
 WHERE p.id = o.id;
ALTER TABLE personnes.personne ENABLE TRIGGER trg_personne_date_maj;

INSERT INTO core.compteur_numero (code, annee, dernier)
SELECT split_part(numero_personne, '-', 1), split_part(numero_personne, '-', 2)::integer, count(*)
  FROM personnes.personne
 GROUP BY 1, 2
ON CONFLICT (code, annee) DO UPDATE SET dernier = GREATEST(core.compteur_numero.dernier, EXCLUDED.dernier);

ALTER TABLE personnes.personne ALTER COLUMN numero_personne SET NOT NULL;
ALTER TABLE personnes.personne ADD CONSTRAINT uk_personne_numero UNIQUE (numero_personne);

CREATE FUNCTION core.attribuer_numero_personne() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER SET search_path = core, pg_temp
AS $$
BEGIN
    -- Toujours attribue par le serveur, meme si la requete en fournit un.
    NEW.numero_personne := core.prochain_numero(CASE NEW.type_personne WHEN 'PHYSIQUE' THEN 'PERS' ELSE 'ORG' END);
    RETURN NEW;
END
$$;
CREATE TRIGGER trg_personne_numero BEFORE INSERT ON personnes.personne
    FOR EACH ROW EXECUTE FUNCTION core.attribuer_numero_personne();

-- 3. Dossiers --------------------------------------------------------------------------------------------
-- Les numeros deja attribues ne sont JAMAIS renumerotes (ils ont pu etre communiques). Seuls sont remplaces :
-- les numeros absents et les doublons (le doublon le plus recent recoit un nouveau numero).
UPDATE dossiers.dossier d
   SET numero_dossier = core.prochain_numero('DOSS')
 WHERE d.id IN (
        SELECT id FROM (
            SELECT id,
                   row_number() OVER (PARTITION BY numero_dossier ORDER BY date_creation, id) AS rang
              FROM dossiers.dossier
             WHERE numero_dossier IS NOT NULL AND btrim(numero_dossier) <> ''
        ) t WHERE rang > 1
   );
ALTER TABLE dossiers.dossier DISABLE TRIGGER trg_dossier_date_maj;
UPDATE dossiers.dossier d
   SET numero_dossier = core.prochain_numero('DOSS')
 WHERE d.numero_dossier IS NULL OR btrim(d.numero_dossier) = '';
ALTER TABLE dossiers.dossier ENABLE TRIGGER trg_dossier_date_maj;

ALTER TABLE dossiers.dossier ALTER COLUMN numero_dossier SET NOT NULL;
ALTER TABLE dossiers.dossier ADD CONSTRAINT uk_dossier_numero UNIQUE (numero_dossier);

CREATE FUNCTION core.attribuer_numero_dossier() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER SET search_path = core, pg_temp
AS $$
BEGIN
    NEW.numero_dossier := core.prochain_numero('DOSS');
    RETURN NEW;
END
$$;
CREATE TRIGGER trg_dossier_numero BEFORE INSERT ON dossiers.dossier
    FOR EACH ROW EXECUTE FUNCTION core.attribuer_numero_dossier();

-- 4. Droits : l'application ne peut ni lire/modifier le compteur, ni consommer des numeros a la main.
REVOKE ALL ON FUNCTION core.prochain_numero(text) FROM PUBLIC;
REVOKE ALL ON FUNCTION core.attribuer_numero_personne() FROM PUBLIC;
REVOKE ALL ON FUNCTION core.attribuer_numero_dossier() FROM PUBLIC;
REVOKE ALL ON core.compteur_numero FROM PUBLIC;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'logintegrite_app') THEN
        REVOKE ALL ON core.compteur_numero FROM logintegrite_app;
        REVOKE ALL ON FUNCTION core.prochain_numero(text) FROM logintegrite_app;
        REVOKE ALL ON FUNCTION core.attribuer_numero_personne() FROM logintegrite_app;
        REVOKE ALL ON FUNCTION core.attribuer_numero_dossier() FROM logintegrite_app;
    END IF;
END
$$;
