# Etat trimestriel des personnes fichees - conception

## Objectif
Produire automatiquement, a la fin de chaque trimestre, un etat officiel des personnes fichees destine a la hierarchie, l'archiver tel que remis (non modifiable, empreinte SHA-256) et pouvoir le regenerer a la demande.

## Hypotheses validees
- Destinataire: la hierarchie. Livrable: PDF officiel + export Excel du meme etat.
- Contenu (option B): synthese du trimestre + liste complete des personnes fichees a la date d'arret.
- Pas d'envoi e-mail ni de signature electronique (hors perimetre).

## Existant reutilise
- `RapportService.genererPdfRegistreOfficiel()` / `PdfEcriture` (PDFBox), POI pour Excel, `RapportController` (`/api/v1/rapports`).
- Modele de tache planifiee: `ExpirationComptesJob`.
- Changements de statut judiciaire traces dans `audit.journal_audit` (cf. `PersonneServiceImpl.historiqueStatutsJudiciaires`).

## Conception
1. **Table `etat_trimestriel`** (migration V13): id, annee, trimestre (1-4), date_arret, date_generation, genere_par (utilisateur ou "SYSTEME"), pdf (bytea), excel (bytea), sha256_pdf, sha256_excel, remplace (boolean). Un seul etat actif par (annee, trimestre); une regeneration marque l'ancien `remplace = true` et le conserve.
2. **Contenu du PDF**: en-tete (trimestre, date d'arret), page de synthese (nouvelles personnes fichees, changements de statut judiciaire, dossiers clos sur le trimestre, calcules depuis `journal_audit` et les dates de creation/cloture), liste complete a la date d'arret, empreinte en pied de page. L'Excel reprend synthese et liste.
3. **Planification**: tache `@Scheduled` (cron) le lendemain de la fin de trimestre, generant l'etat du trimestre ecoule. Au demarrage, rattrapage des trimestres manquants.
4. **API**: `GET /api/v1/rapports/etats-trimestriels` (liste), `GET .../{id}/pdf`, `GET .../{id}/excel`, `POST .../generer` (ADMIN). Lecture: ADMIN et VALIDATEUR.
5. **Frontend**: page « Etats trimestriels » (liste, telechargement PDF/Excel, bouton « Generer maintenant » pour l'administrateur).
6. **Tests (TDD)**: calcul du trimestre et de la date d'arret, calcul de la synthese, unicite et remplacement, rattrapage, droits d'acces.

## Questions ouvertes
- Visibilite pour les consultants: par defaut non (ADMIN et VALIDATEUR seulement).
