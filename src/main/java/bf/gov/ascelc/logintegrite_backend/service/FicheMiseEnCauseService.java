package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.FicheMiseEnCauseResponse;
import java.util.UUID;

public interface FicheMiseEnCauseService {

    // ── CONTRAT DES ENTITÉS (Pour la logique métier interne) ──
    FicheMiseEnCause consulterAvecDetails(UUID id);
    FicheMiseEnCause consulter(UUID id);
    FicheMiseEnCause soumettre(UUID id, String agentId);
    FicheMiseEnCause valider(UUID id, String validateurId);
    FicheMiseEnCause rejeter(UUID id, String motif, String validateurId);
    FicheMiseEnCause archiver(UUID id);
    FicheMiseEnCause modifierStatutJudiciaire(UUID id, StatutJudiciaireRequest request, String agentId);

    // ── CONTRAT DES DTOS (Pour l'exposition saine aux Contrôleurs) ──
    FicheMiseEnCauseResponse obtenirFichePourAffichage(UUID id);
    FicheMiseEnCauseResponse soumettreFiche(UUID id, String agentId);
    FicheMiseEnCauseResponse validerFiche(UUID id, String validateurId);
    FicheMiseEnCauseResponse rejeterFiche(UUID id, String motif, String validateurId);
    FicheMiseEnCauseResponse modifierStatutJudiciaireFiche(UUID id, StatutJudiciaireRequest request, String agentId);

    long countEnAttente();
}