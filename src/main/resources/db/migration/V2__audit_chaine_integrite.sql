-- V2 : audit forensique.
--   * chaine de hachage SHA-256 chainee sur audit.journal_audit (calculee par trigger)
--   * immuabilite (UPDATE / DELETE / TRUNCATE interdits) du journal d'audit, du journal de
--     consultation et des documents
--   * fonctions de verification appelees par JournalAuditForensiqueRepository
-- La formule de hash n'existe qu'a un seul endroit : audit.calculer_hash().

-- ── Ordre de la chaine ────────────────────────────────────────────────────
-- Le numero de sequence est attribue DANS le trigger, apres prise du verrou, afin que l'ordre
-- des seq soit exactement l'ordre de chainage meme en cas d'insertions concurrentes.
CREATE SEQUENCE audit.journal_audit_seq;
ALTER TABLE audit.journal_audit ADD COLUMN seq bigint;
CREATE UNIQUE INDEX ux_journal_audit_seq ON audit.journal_audit (seq);
CREATE INDEX ix_journal_audit_date_action ON audit.journal_audit (date_action);
CREATE INDEX ix_journal_audit_cible ON audit.journal_audit (entite_cible, entite_cible_id);
CREATE INDEX ix_journal_audit_utilisateur ON audit.journal_audit (utilisateur_id, date_action);
CREATE INDEX ix_journal_consultation_date ON audit.journal_consultation (date_consultation);

-- ── Formule de hash (source unique de verite) ────────────────────────────
CREATE FUNCTION audit.calculer_hash(
    p_precedent   text,
    p_id          uuid,
    p_utilisateur uuid,
    p_action      text,
    p_cible       text,
    p_cible_id    uuid,
    p_avant       jsonb,
    p_apres       jsonb,
    p_date        timestamptz
) RETURNS text
LANGUAGE sql IMMUTABLE AS $$
    SELECT encode(
        sha256(convert_to(concat_ws('|',
            coalesce(p_precedent, ''),
            p_id::text,
            coalesce(p_utilisateur::text, ''),
            p_action,
            p_cible,
            coalesce(p_cible_id::text, ''),
            coalesce(p_avant::text, ''),
            coalesce(p_apres::text, ''),
            to_char(p_date AT TIME ZONE 'UTC', 'YYYY-MM-DD"T"HH24:MI:SS.US')
        ), 'UTF8')),
        'hex')
$$;

-- ── Trigger de chainage ──────────────────────────────────────────────────
CREATE FUNCTION audit.journal_audit_chainer() RETURNS trigger
LANGUAGE plpgsql AS $$
DECLARE
    v_precedent text;
BEGIN
    -- Serialise les insertions : un seul maillon a la fois peut se rattacher a la fin de chaine.
    PERFORM pg_advisory_xact_lock(hashtext('audit.journal_audit'));

    SELECT hash_actuel INTO v_precedent
    FROM audit.journal_audit
    ORDER BY seq DESC
    LIMIT 1;

    NEW.seq             := nextval('audit.journal_audit_seq');
    NEW.hash_precedent  := coalesce(v_precedent, repeat('0', 64));
    NEW.hash_actuel     := audit.calculer_hash(
        NEW.hash_precedent, NEW.id, NEW.utilisateur_id, NEW.action, NEW.entite_cible,
        NEW.entite_cible_id, NEW.valeur_avant, NEW.valeur_apres, NEW.date_action);
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_journal_audit_chainer
    BEFORE INSERT ON audit.journal_audit
    FOR EACH ROW EXECUTE FUNCTION audit.journal_audit_chainer();

-- ── Immuabilite ──────────────────────────────────────────────────────────
CREATE FUNCTION audit.interdire_modification() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'Table %.% immuable : % interdit', TG_TABLE_SCHEMA, TG_TABLE_NAME, TG_OP
        USING ERRCODE = 'integrity_constraint_violation';
END;
$$;

CREATE TRIGGER trg_journal_audit_immuable
    BEFORE UPDATE OR DELETE ON audit.journal_audit
    FOR EACH ROW EXECUTE FUNCTION audit.interdire_modification();
CREATE TRIGGER trg_journal_audit_no_truncate
    BEFORE TRUNCATE ON audit.journal_audit
    FOR EACH STATEMENT EXECUTE FUNCTION audit.interdire_modification();

CREATE TRIGGER trg_journal_consultation_immuable
    BEFORE UPDATE OR DELETE ON audit.journal_consultation
    FOR EACH ROW EXECUTE FUNCTION audit.interdire_modification();
CREATE TRIGGER trg_journal_consultation_no_truncate
    BEFORE TRUNCATE ON audit.journal_consultation
    FOR EACH STATEMENT EXECUTE FUNCTION audit.interdire_modification();

CREATE TRIGGER trg_document_immuable
    BEFORE UPDATE OR DELETE ON documents.document
    FOR EACH ROW EXECUTE FUNCTION audit.interdire_modification();
CREATE TRIGGER trg_document_no_truncate
    BEFORE TRUNCATE ON documents.document
    FOR EACH STATEMENT EXECUTE FUNCTION audit.interdire_modification();

-- ── Fonctions forensiques ────────────────────────────────────────────────
CREATE FUNCTION audit.etat_chaine()
RETURNS TABLE (
    total_entrees        bigint,
    dernier_hash         text,
    date_derniere_action timestamptz,
    date_premiere_action timestamptz,
    total_utilisateurs   bigint,
    total_entites        bigint
)
LANGUAGE sql STABLE AS $$
    SELECT count(*),
           (SELECT j.hash_actuel FROM audit.journal_audit j ORDER BY j.seq DESC LIMIT 1),
           max(date_action),
           min(date_action),
           count(DISTINCT utilisateur_id),
           count(DISTINCT entite_cible)
    FROM audit.journal_audit
$$;

-- Ne renvoie que les maillons rompus (liste vide = chaine intacte).
-- p_limite : nombre maximum de maillons verifies depuis le debut de la chaine (NULL = tous).
CREATE FUNCTION audit.verifier_integrite_chaine(p_limite bigint DEFAULT NULL)
RETURNS TABLE (
    id                     uuid,
    date_action            timestamptz,
    action                 text,
    entite_cible           text,
    hash_attendu           text,
    hash_stocke            text,
    hash_precedent         text,
    hash_precedent_attendu text,
    type_rupture           text
)
LANGUAGE sql STABLE AS $$
    WITH chaine AS (
        SELECT j.*,
               row_number() OVER (ORDER BY j.seq)                                   AS rang,
               coalesce(lag(j.hash_actuel) OVER (ORDER BY j.seq), repeat('0', 64)) AS precedent_reel
        FROM audit.journal_audit j
    ),
    controle AS (
        SELECT c.*,
               audit.calculer_hash(c.hash_precedent, c.id, c.utilisateur_id, c.action, c.entite_cible,
                                   c.entite_cible_id, c.valeur_avant, c.valeur_apres, c.date_action) AS hash_calcule
        FROM chaine c
        WHERE p_limite IS NULL OR c.rang <= p_limite
    )
    SELECT c.id, c.date_action, c.action::text, c.entite_cible::text,
           c.hash_calcule,
           c.hash_actuel::text,
           c.hash_precedent::text,
           c.precedent_reel,
           CASE WHEN c.hash_calcule IS DISTINCT FROM c.hash_actuel THEN 'HASH_ALTERE'
                ELSE 'CHAINAGE_ROMPU' END
    FROM controle c
    WHERE c.hash_calcule IS DISTINCT FROM c.hash_actuel
       OR c.hash_precedent IS DISTINCT FROM c.precedent_reel
    ORDER BY c.rang
$$;

-- Localise un maillon par son hash (complet ou prefixe hexadecimal).
CREATE FUNCTION audit.verifier_maillon(p_hash text)
RETURNS TABLE (
    id                   uuid,
    date_action          timestamptz,
    action               text,
    entite_cible         text,
    hash_stocke          text,
    hash_recalcule       text,
    hash_precedent       text,
    integre              boolean,
    position_dans_chaine bigint
)
LANGUAGE sql STABLE AS $$
    WITH chaine AS (
        SELECT j.*,
               row_number() OVER (ORDER BY j.seq)                                   AS rang,
               coalesce(lag(j.hash_actuel) OVER (ORDER BY j.seq), repeat('0', 64)) AS precedent_reel
        FROM audit.journal_audit j
    )
    SELECT c.id, c.date_action, c.action::text, c.entite_cible::text,
           c.hash_actuel::text,
           audit.calculer_hash(c.hash_precedent, c.id, c.utilisateur_id, c.action, c.entite_cible,
                               c.entite_cible_id, c.valeur_avant, c.valeur_apres, c.date_action),
           c.hash_precedent::text,
           audit.calculer_hash(c.hash_precedent, c.id, c.utilisateur_id, c.action, c.entite_cible,
                               c.entite_cible_id, c.valeur_avant, c.valeur_apres, c.date_action) = c.hash_actuel
             AND c.hash_precedent = c.precedent_reel,
           c.rang
    FROM chaine c
    WHERE c.hash_actuel = p_hash OR c.hash_actuel LIKE p_hash || '%'
    ORDER BY c.rang
    LIMIT 1
$$;

CREATE FUNCTION audit.kpi_forensique(p_heure_ouverture integer DEFAULT 7, p_heure_fermeture integer DEFAULT 18)
RETURNS TABLE (
    actions_aujourdhui_total      bigint,
    actions_aujourdhui_creation   bigint,
    actions_aujourdhui_modif      bigint,
    actions_aujourdhui_suppr      bigint,
    delta_actions_vs_hier         bigint,
    consultations_24h             bigint,
    consultations_24h_precedentes bigint,
    delta_consultations_pct       numeric,
    utilisateurs_actifs_24h       bigint,
    utilisateurs_actifs_7j        bigint,
    alertes_ouvertes              bigint,
    actions_hors_horaire_24h      bigint,
    ips_multiples_24h             bigint
)
LANGUAGE sql STABLE AS $$
    WITH bornes AS (
        SELECT date_trunc('day', now())  AS debut_jour,
               now() - interval '24 hours' AS il_y_a_24h,
               now() - interval '48 hours' AS il_y_a_48h,
               now() - interval '7 days'   AS il_y_a_7j
    ),
    aujourdhui AS (
        SELECT a.* FROM audit.journal_audit a, bornes b WHERE a.date_action >= b.debut_jour
    ),
    hier AS (
        SELECT count(*) AS n FROM audit.journal_audit a, bornes b
        WHERE a.date_action >= b.debut_jour - interval '1 day' AND a.date_action < b.debut_jour
    ),
    cons AS (
        SELECT count(*) FILTER (WHERE c.date_consultation >= b.il_y_a_24h) AS n24,
               count(*) FILTER (WHERE c.date_consultation >= b.il_y_a_48h
                                  AND c.date_consultation <  b.il_y_a_24h) AS nprec
        FROM audit.journal_consultation c, bornes b
        WHERE c.date_consultation >= b.il_y_a_48h
    ),
    actifs AS (
        SELECT u.utilisateur_id, u.horodatage FROM (
            SELECT utilisateur_id, date_action AS horodatage FROM audit.journal_audit
            UNION ALL
            SELECT utilisateur_id, date_consultation FROM audit.journal_consultation
        ) u, bornes b
        WHERE u.horodatage >= b.il_y_a_7j AND u.utilisateur_id IS NOT NULL
    ),
    -- Alertes : maillons rompus parmi les 1000 dernieres entrees de la chaine.
    fenetre AS (
        SELECT j.*, lag(j.hash_actuel) OVER (ORDER BY j.seq) AS precedent_reel
        FROM (SELECT * FROM audit.journal_audit ORDER BY seq DESC LIMIT 1001) j
    ),
    alertes AS (
        SELECT count(*) AS n FROM fenetre f
        WHERE f.precedent_reel IS NOT NULL
          AND (f.hash_precedent IS DISTINCT FROM f.precedent_reel
               OR f.hash_actuel IS DISTINCT FROM audit.calculer_hash(f.hash_precedent, f.id, f.utilisateur_id,
                    f.action, f.entite_cible, f.entite_cible_id, f.valeur_avant, f.valeur_apres, f.date_action))
    )
    SELECT (SELECT count(*) FROM aujourdhui),
           (SELECT count(*) FROM aujourdhui WHERE action LIKE 'CREATION%' OR action IN ('OUVERTURE_DOSSIER', 'AJOUT_DOSSIER_PERSONNE', 'DEPOT')),
           (SELECT count(*) FROM aujourdhui WHERE action LIKE 'MODIFICATION%' OR action IN ('VALIDATION', 'REJET', 'REPRISE_FAIT_REJETE', 'ATTRIBUTION_ROLE', 'TAG_DOCUMENT')),
           (SELECT count(*) FROM aujourdhui WHERE action LIKE 'SUPPRESSION%' OR action LIKE 'RETRAIT%'),
           (SELECT count(*) FROM aujourdhui) - (SELECT n FROM hier),
           (SELECT coalesce(n24, 0) FROM cons),
           (SELECT coalesce(nprec, 0) FROM cons),
           (SELECT CASE WHEN coalesce(nprec, 0) = 0 THEN NULL
                        ELSE round(100.0 * (n24 - nprec) / nprec, 1) END FROM cons),
           (SELECT count(DISTINCT utilisateur_id) FROM actifs a, bornes b WHERE a.horodatage >= b.il_y_a_24h),
           (SELECT count(DISTINCT utilisateur_id) FROM actifs),
           (SELECT n FROM alertes),
           (SELECT count(*) FROM audit.journal_audit a, bornes b
             WHERE a.date_action >= b.il_y_a_24h
               AND (extract(hour FROM a.date_action) < p_heure_ouverture
                    OR extract(hour FROM a.date_action) >= p_heure_fermeture)),
           (SELECT count(*) FROM (
                SELECT utilisateur_id FROM audit.journal_audit a, bornes b
                WHERE a.date_action >= b.il_y_a_24h AND a.utilisateur_id IS NOT NULL AND a.adresse_ip IS NOT NULL
                GROUP BY utilisateur_id HAVING count(DISTINCT adresse_ip) > 1) multi)
$$;
