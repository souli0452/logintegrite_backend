-- Une implication sans statut alors que ses faits en portent un affichait « Aucun statut » sur la fiche de la
-- personne, mais un statut sur la fiche de verification. On aligne l'implication sur son statut le plus recent.
UPDATE dossiers.implication i
SET statut_judiciaire_id = (
    SELECT imf.statut_judiciaire_id
    FROM dossiers.implication_fait imf
    WHERE imf.implication_id = i.id
    ORDER BY imf.date_statut DESC NULLS LAST
    LIMIT 1)
WHERE i.statut_judiciaire_id IS NULL
  AND EXISTS (SELECT 1 FROM dossiers.implication_fait imf WHERE imf.implication_id = i.id);
