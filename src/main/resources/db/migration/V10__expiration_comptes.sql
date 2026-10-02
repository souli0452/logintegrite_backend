-- V10 : date d'expiration d'un compte. Obligatoire pour un compte de consultation (regle appliquee par le service), facultative
-- pour les autres. A l'echeance, le compte est refuse immediatement par l'API puis desactive (application et Keycloak).
ALTER TABLE securite.utilisateur ADD COLUMN date_expiration date;

CREATE INDEX idx_utilisateur_expiration ON securite.utilisateur (date_expiration) WHERE date_expiration IS NOT NULL;
