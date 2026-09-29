-- V3 : donnees de reference de depart (Burkina Faso / ASCE-LC).
-- Ces listes sont administrables ensuite depuis l'application (ecrans Referentiels, role ADMIN).
-- Les libelles d'entites administratives sont indicatifs : a ajuster par l'ASCE-LC.

-- ── Roles d'habilitation (alignes sur core.code_role et les roles Keycloak) ──
INSERT INTO securite.role_habilitation (code, libelle, acces_vue_globale_dossier) VALUES
    ('ADMIN',      'Administrateur',      true),
    ('VALIDATEUR', 'Validateur',          true),
    ('AGENT',      'Agent de saisie',     false),
    ('CONSULTANT', 'Consultant (lecture)', false);

-- ── Zones geographiques ─────────────────────────────────────────────────
INSERT INTO referentiels.zone_geographique (code, libelle, niveau, parent_id) VALUES
    ('BF', 'Burkina Faso', 'PAYS', NULL);

INSERT INTO referentiels.zone_geographique (code, libelle, niveau, parent_id)
SELECT r.code, r.libelle, 'REGION', p.id
FROM (VALUES
    ('BMH', 'Boucle du Mouhoun'),
    ('CAS', 'Cascades'),
    ('CEN', 'Centre'),
    ('CES', 'Centre-Est'),
    ('CNO', 'Centre-Nord'),
    ('COU', 'Centre-Ouest'),
    ('CSU', 'Centre-Sud'),
    ('EST', 'Est'),
    ('HBA', 'Hauts-Bassins'),
    ('NOR', 'Nord'),
    ('PCE', 'Plateau-Central'),
    ('SAH', 'Sahel'),
    ('SOU', 'Sud-Ouest')
) AS r(code, libelle)
CROSS JOIN referentiels.zone_geographique p
WHERE p.code = 'BF';

INSERT INTO referentiels.zone_geographique (code, libelle, niveau, parent_id)
SELECT 'KAD', 'Kadiogo', 'PROVINCE', id FROM referentiels.zone_geographique WHERE code = 'CEN';
INSERT INTO referentiels.zone_geographique (code, libelle, niveau, parent_id)
SELECT 'HOU', 'Houet', 'PROVINCE', id FROM referentiels.zone_geographique WHERE code = 'HBA';
INSERT INTO referentiels.zone_geographique (code, libelle, niveau, parent_id)
SELECT 'OUA', 'Ouagadougou', 'COMMUNE', id FROM referentiels.zone_geographique WHERE code = 'KAD';
INSERT INTO referentiels.zone_geographique (code, libelle, niveau, parent_id)
SELECT 'BOB', 'Bobo-Dioulasso', 'COMMUNE', id FROM referentiels.zone_geographique WHERE code = 'HOU';

-- ── Entites de l'administration ─────────────────────────────────────────
INSERT INTO referentiels.entite_organisation (libelle, niveau, parent_id) VALUES
    ('Ministère de l''Économie et des Finances', 'MINISTERE', NULL),
    ('Ministère de la Justice',                  'MINISTERE', NULL),
    ('Ministère de l''Administration territoriale', 'MINISTERE', NULL),
    ('Ministère de la Santé',                    'MINISTERE', NULL),
    ('Ministère de l''Éducation nationale',      'MINISTERE', NULL),
    ('Ministère des Infrastructures',            'MINISTERE', NULL),
    ('Ministère de l''Énergie et des Mines',     'MINISTERE', NULL);

INSERT INTO referentiels.entite_organisation (libelle, niveau, parent_id)
SELECT d.libelle, 'DIRECTION', m.id
FROM (VALUES
    ('Direction générale des Impôts',                           'Ministère de l''Économie et des Finances'),
    ('Direction générale des Douanes',                          'Ministère de l''Économie et des Finances'),
    ('Direction générale du Trésor et de la Comptabilité publique', 'Ministère de l''Économie et des Finances'),
    ('Direction générale du Budget',                            'Ministère de l''Économie et des Finances'),
    ('Direction des Affaires pénales et de la Grâce',           'Ministère de la Justice'),
    ('Direction générale de l''Administration territoriale',    'Ministère de l''Administration territoriale'),
    ('Direction générale de la Pharmacie et des Laboratoires',  'Ministère de la Santé'),
    ('Direction des Marchés publics',                           'Ministère des Infrastructures')
) AS d(libelle, ministere)
JOIN referentiels.entite_organisation m ON m.libelle = d.ministere AND m.niveau = 'MINISTERE';

INSERT INTO referentiels.entite_organisation (libelle, niveau, parent_id)
SELECT 'Service de recouvrement', 'SERVICE', id
FROM referentiels.entite_organisation WHERE libelle = 'Direction générale des Impôts';
INSERT INTO referentiels.entite_organisation (libelle, niveau, parent_id)
SELECT 'Service de la commande publique', 'SERVICE', id
FROM referentiels.entite_organisation WHERE libelle = 'Direction des Marchés publics';

-- ── Categories et types d'infraction ────────────────────────────────────
INSERT INTO referentiels.categorie_infraction (libelle, description) VALUES
    ('Corruption et infractions assimilées', 'Corruption active ou passive, trafic d''influence, prise illégale d''intérêts'),
    ('Atteintes aux deniers publics',        'Détournement, concussion, enrichissement illicite'),
    ('Atteintes à la commande publique',     'Favoritisme, fractionnement, délit d''initié dans les marchés publics'),
    ('Faux et usage de faux',                'Falsification de documents administratifs ou comptables'),
    ('Blanchiment et infractions financières', 'Blanchiment de capitaux, fraude fiscale et douanière');

INSERT INTO referentiels.type_infraction (libelle, actif, categorie_infraction_id)
SELECT t.libelle, true, c.id
FROM (VALUES
    ('Corruption active',                         'Corruption et infractions assimilées'),
    ('Corruption passive',                        'Corruption et infractions assimilées'),
    ('Trafic d''influence',                       'Corruption et infractions assimilées'),
    ('Prise illégale d''intérêts',                'Corruption et infractions assimilées'),
    ('Détournement de deniers publics',           'Atteintes aux deniers publics'),
    ('Concussion',                                'Atteintes aux deniers publics'),
    ('Enrichissement illicite',                   'Atteintes aux deniers publics'),
    ('Favoritisme dans un marché public',         'Atteintes à la commande publique'),
    ('Fractionnement de marché',                  'Atteintes à la commande publique'),
    ('Faux en écriture administrative',           'Faux et usage de faux'),
    ('Usage de faux',                             'Faux et usage de faux'),
    ('Blanchiment de capitaux',                   'Blanchiment et infractions financières'),
    ('Fraude fiscale ou douanière',               'Blanchiment et infractions financières')
) AS t(libelle, categorie)
JOIN referentiels.categorie_infraction c ON c.libelle = t.categorie;

-- ── Statuts judiciaires ─────────────────────────────────────────────────
INSERT INTO referentiels.statut_judiciaire (libelle, actif) VALUES
    ('Enquête préliminaire',            true),
    ('Mise en cause',                   true),
    ('Inculpation',                     true),
    ('Renvoi en jugement',              true),
    ('Condamnation en première instance', true),
    ('Appel en cours',                  true),
    ('Condamnation définitive',         true),
    ('Relaxe ou acquittement',          true),
    ('Non-lieu',                        true),
    ('Classement sans suite',           true);

-- ── Roles d'implication ─────────────────────────────────────────────────
INSERT INTO referentiels.role_implication (libelle, actif) VALUES
    ('Auteur principal', true),
    ('Coauteur',         true),
    ('Complice',         true),
    ('Commanditaire',    true),
    ('Bénéficiaire',     true),
    ('Receleur',         true);

-- ── Sources de signalement ──────────────────────────────────────────────
INSERT INTO referentiels.source_signalement (libelle, description) VALUES
    ('Dénonciation',                        'Dénonciation reçue par l''ASCE-LC'),
    ('Déclaration d''intérêts et de patrimoine', 'Anomalie relevée lors du traitement d''une DIP'),
    ('Contrôle administratif interne',      'Constat lors d''un contrôle du fonctionnement d''un service public'),
    ('Rapport d''audit ou d''inspection',   'Rapport d''un corps de contrôle'),
    ('Autosaisine',                         'Saisine d''office par l''ASCE-LC'),
    ('Transmission judiciaire',             'Information transmise par une juridiction');

-- ── Types de documents ──────────────────────────────────────────────────
INSERT INTO referentiels.type_document (libelle, actif) VALUES
    ('Rapport d''audit',              true),
    ('Procès-verbal',                 true),
    ('Décision de justice',           true),
    ('Plainte ou dénonciation',       true),
    ('Pièce justificative comptable', true),
    ('Correspondance',                true),
    ('Preuve numérique',              true),
    ('Autre',                         true);

-- ── Types de pieces d'identite ──────────────────────────────────────────
INSERT INTO referentiels.type_piece_identite (code, libelle, actif) VALUES
    ('CNIB',     'Carte nationale d''identité burkinabè', true),
    ('PASSEPORT', 'Passeport',                             true),
    ('NIF',      'Numéro d''identification financière unique', true),
    ('PERMIS',   'Permis de conduire',                     true),
    ('CONSULAIRE', 'Carte consulaire',                     true);

-- ── Nationalites ────────────────────────────────────────────────────────
INSERT INTO referentiels.nationalite (libelle, code_iso, actif) VALUES
    ('Burkinabè',  'BFA', true),
    ('Ivoirienne', 'CIV', true),
    ('Malienne',   'MLI', true),
    ('Nigérienne', 'NER', true),
    ('Togolaise',  'TGO', true),
    ('Béninoise',  'BEN', true),
    ('Ghanéenne',  'GHA', true),
    ('Sénégalaise', 'SEN', true),
    ('Guinéenne',  'GIN', true),
    ('Nigériane',  'NGA', true),
    ('Française',  'FRA', true),
    ('Libanaise',  'LBN', true);
