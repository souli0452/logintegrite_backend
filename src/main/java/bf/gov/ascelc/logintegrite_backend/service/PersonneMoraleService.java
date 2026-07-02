package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonneMoraleResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonneMoralePublicResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.VerificationExistenceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PersonneMoraleService extends FicheMiseEnCauseService {

    PersonneMoraleResponse creerFiche(PersonneMoraleRequest request);
    PersonneMoraleResponse modifierFiche(UUID id, PersonneMoraleRequest request);
    Page<PersonneMoraleResponse> rechercherFichesInterne(String raisonSociale, UUID entiteId, UUID regionId, String statut, String typeStructure, Pageable pageable);
    Page<PersonneMoralePublicResponse> rechercherFichesPublic(String raisonSociale, UUID entiteId, UUID regionId, String statut, String typeStructure, Pageable pageable);

    PersonneMoralePublicResponse obtenirFichePourAffichagePublic(UUID id);

    VerificationExistenceResponse verifierExistence(String ifu, String raisonSociale);

    Page<PersonneMoraleResponse> rechercherMesFiches(String userId, String statut, Pageable pageable);

    List<PersonneMorale> listerRecentesBrouillonsOuSoumises(String userId, int limite);

    PersonneMorale creer(PersonneMorale pm);
    PersonneMorale modifier(UUID id, PersonneMorale pm);
    List<PersonneMorale> listerTout();

    @Override PersonneMorale consulter(UUID id);
    @Override PersonneMorale soumettre(UUID id, String agentId);
    @Override PersonneMorale valider(UUID id, String validateurId);
    @Override PersonneMorale rejeter(UUID id, String motif, String validateurId);
    @Override PersonneMorale archiver(UUID id);
    @Override PersonneMorale modifierStatutJudiciaire(UUID id, StatutJudiciaireRequest request, String agentId);

    @Override PersonneMoraleResponse obtenirFichePourAffichage(UUID id);
    @Override PersonneMoraleResponse soumettreFiche(UUID id, String agentId);
    @Override PersonneMoraleResponse validerFiche(UUID id, String validateurId);
    @Override PersonneMoraleResponse rejeterFiche(UUID id, String motif, String validateurId);
    @Override PersonneMoraleResponse modifierStatutJudiciaireFiche(UUID id, StatutJudiciaireRequest request, String agentId);
}