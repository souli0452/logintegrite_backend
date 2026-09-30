-- Un utilisateur ne peut pas avoir deux fois le meme role.
-- Sans cette contrainte, deux requetes simultanees (ou deux clics) pouvaient creer des doublons.

-- 1. Supprimer les doublons existants en gardant la plus ancienne attribution (identifiants v7 : ordre chronologique).
DELETE FROM securite.utilisateur_role a
      USING securite.utilisateur_role b
WHERE a.utilisateur_id = b.utilisateur_id
  AND a.role_habilitation_id = b.role_habilitation_id
  AND a.id > b.id;

-- 2. Interdire les doublons a l'avenir.
ALTER TABLE securite.utilisateur_role
    ADD CONSTRAINT uk_utilisateur_role UNIQUE (utilisateur_id, role_habilitation_id);
