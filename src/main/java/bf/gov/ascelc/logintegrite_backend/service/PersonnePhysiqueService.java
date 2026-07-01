package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonnePhysiqueResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonnePhysiquePublicResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PersonnePhysiqueService extends FicheMiseEnCauseService {

    // Métodes CRUD exclusives à la Personne Physique
    PersonnePhysiqueResponse creerFiche(PersonnePhysiqueRequest request);
    PersonnePhysiqueResponse modifierFiche(UUID id, PersonnePhysiqueRequest request);
    Page<PersonnePhysiqueResponse> rechercherFichesInterne(String nom, UUID entiteId, UUID regionId, String statut, Pageable pageable);
    Page<PersonnePhysiquePublicResponse> rechercherFichesPublic(String nom, UUID entiteId, UUID regionId, String statut, Pageable pageable);

    // AJOUT : consultation par ID pour le rôle public.
    // obtenirFichePourAffichage() renvoie toujours le DTO complet (PII incluse),
    // il ne doit donc plus être appelé pour un utilisateur public. Cette méthode
    // renvoie le DTO restreint et refuse les fiches qui ne sont pas ACTIVE.
    PersonnePhysiquePublicResponse obtenirFichePourAffichagePublic(UUID id);

    PersonnePhysique creer(PersonnePhysique pp);
    PersonnePhysique modifier(UUID id, PersonnePhysique pp);
    List<PersonnePhysique> listerTout();

    // ── COVARIANCE STRICTE DES ENTITÉS ──
    @Override PersonnePhysique consulter(UUID id);
    @Override PersonnePhysique soumettre(UUID id, String agentId);
    @Override PersonnePhysique valider(UUID id, String validateurId);
    @Override PersonnePhysique rejeter(UUID id, String motif, String validateurId);
    @Override PersonnePhysique archiver(UUID id);
    @Override PersonnePhysique modifierStatutJudiciaire(UUID id, StatutJudiciaireRequest request, String agentId);

    // ── COVARIANCE STRICTE DES DTOS ──
    @Override PersonnePhysiqueResponse obtenirFichePourAffichage(UUID id);
    @Override PersonnePhysiqueResponse soumettreFiche(UUID id, String agentId);
    @Override PersonnePhysiqueResponse validerFiche(UUID id, String validateurId);
    @Override PersonnePhysiqueResponse rejeterFiche(UUID id, String motif, String validateurId);
    @Override PersonnePhysiqueResponse modifierStatutJudiciaireFiche(UUID id, StatutJudiciaireRequest request, String agentId);
}
