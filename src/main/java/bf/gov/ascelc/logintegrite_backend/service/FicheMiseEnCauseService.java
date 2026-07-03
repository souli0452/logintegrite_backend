package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.FicheMiseEnCauseResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FicheMiseEnCauseService {

    FicheMiseEnCause consulterAvecDetails(UUID id);
    FicheMiseEnCause consulter(UUID id);
    FicheMiseEnCause soumettre(UUID id, String agentId);
    FicheMiseEnCause valider(UUID id, String validateurId);
    FicheMiseEnCause rejeter(UUID id, String motif, String validateurId);
    FicheMiseEnCause archiver(UUID id);
    FicheMiseEnCause modifierStatutJudiciaire(UUID id, StatutJudiciaireRequest request, String agentId);
    FicheMiseEnCause supprimerBrouillonOuSoumise(UUID id, String userId, boolean isAdmin);

    Page<FicheMiseEnCauseResponse> rechercherRegistreOfficiel(String recherche, UUID regionId, UUID entiteId, Pageable pageable);

    // AJOUT : file d'attente de validation — fiches EN_ATTENTE_VALIDATION,
    // PP+PM confondus, tous créateurs confondus. Réutilise la requête
    // rechercheGlobale déjà présente dans FicheMiseEnCauseRepository.
    Page<FicheMiseEnCauseResponse> rechercherFileAttenteValidation(UUID regionId, UUID entiteId, Pageable pageable);

    FicheMiseEnCauseResponse obtenirFichePourAffichage(UUID id);
    FicheMiseEnCauseResponse soumettreFiche(UUID id, String agentId);
    FicheMiseEnCauseResponse validerFiche(UUID id, String validateurId);
    FicheMiseEnCauseResponse rejeterFiche(UUID id, String motif, String validateurId);
    FicheMiseEnCauseResponse modifierStatutJudiciaireFiche(UUID id, StatutJudiciaireRequest request, String agentId);

    long countEnAttente();
}