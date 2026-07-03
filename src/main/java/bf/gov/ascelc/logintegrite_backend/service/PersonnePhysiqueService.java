package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonnePhysiqueResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonnePhysiquePublicResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.VerificationExistenceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PersonnePhysiqueService extends FicheMiseEnCauseService {

    PersonnePhysiqueResponse creerFiche(PersonnePhysiqueRequest request);
    PersonnePhysiqueResponse modifierFiche(UUID id, PersonnePhysiqueRequest request);
    Page<PersonnePhysiqueResponse> rechercherFichesInterne(String nom, UUID entiteId, UUID regionId, String statut, Pageable pageable);
    Page<PersonnePhysiquePublicResponse> rechercherFichesPublic(String nom, UUID entiteId, UUID regionId, String statut, Pageable pageable);

    PersonnePhysiquePublicResponse obtenirFichePourAffichagePublic(UUID id);

    VerificationExistenceResponse verifierExistence(String matricule, String nom, String prenoms, LocalDate dateNaissance);

    Page<PersonnePhysiqueResponse> rechercherMesFiches(String userId, String statut, Pageable pageable);

    List<PersonnePhysique> listerRecentesBrouillonsOuSoumises(String userId, int limite);
    // AJOUT : fiches PP validées/rejetées PAR ce validateur — pour ses "actions rapides"
    List<PersonnePhysique> listerRecentesValideesOuRejeteesParValidateur(String validateurId, int limite);

    PersonnePhysique creer(PersonnePhysique pp);
    PersonnePhysique modifier(UUID id, PersonnePhysique pp);
    List<PersonnePhysique> listerTout();

    @Override PersonnePhysique consulter(UUID id);
    @Override PersonnePhysique soumettre(UUID id, String agentId);
    @Override PersonnePhysique valider(UUID id, String validateurId);
    @Override PersonnePhysique rejeter(UUID id, String motif, String validateurId);
    @Override PersonnePhysique archiver(UUID id);
    @Override PersonnePhysique modifierStatutJudiciaire(UUID id, StatutJudiciaireRequest request, String agentId);

    @Override PersonnePhysiqueResponse obtenirFichePourAffichage(UUID id);
    @Override PersonnePhysiqueResponse soumettreFiche(UUID id, String agentId);
    @Override PersonnePhysiqueResponse validerFiche(UUID id, String validateurId);
    @Override PersonnePhysiqueResponse rejeterFiche(UUID id, String motif, String validateurId);
    @Override PersonnePhysiqueResponse modifierStatutJudiciaireFiche(UUID id, StatutJudiciaireRequest request, String agentId);
}