CREATE UNIQUE INDEX uk_fiche_matricule_active
    ON fiche_mise_en_cause (matricule)
    WHERE statut_fiche = 'ACTIVE'
      AND type_fiche = 'PERSONNE_PHYSIQUE'
      AND matricule IS NOT NULL
      AND deleted = false;

CREATE UNIQUE INDEX uk_fiche_ifu_active
    ON fiche_mise_en_cause (ifu)
    WHERE statut_fiche = 'ACTIVE'
      AND type_fiche = 'PERSONNE_MORALE'
      AND ifu IS NOT NULL
      AND deleted = false;
