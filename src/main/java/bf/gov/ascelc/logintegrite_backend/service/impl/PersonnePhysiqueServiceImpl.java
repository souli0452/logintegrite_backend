package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.FicheMiseEnCauseResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonnePhysiqueResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonnePhysiquePublicResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.VerificationExistenceResponse;
import bf.gov.ascelc.logintegrite_backend.exception.FicheDejaExistanteException;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.repository.PersonnePhysiqueRepository;
import bf.gov.ascelc.logintegrite_backend.repository.RegionRepository;
import bf.gov.ascelc.logintegrite_backend.repository.EntiteOrganisationRepository;
import bf.gov.ascelc.logintegrite_backend.service.PersonnePhysiqueService;
import bf.gov.ascelc.logintegrite_backend.mapper.PersonnePhysiqueMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonnePhysiqueServiceImpl implements PersonnePhysiqueService {

    private final PersonnePhysiqueRepository ppRepo;
    private final RegionRepository regionRepo;
    private final EntiteOrganisationRepository entiteRepo;
    private final PersonnePhysiqueMapper ppMapper;

    private final FicheMiseEnCauseServiceImpl ficheMiseEnCauseServiceImpl;

    @Override
    @Transactional
    public PersonnePhysiqueResponse creerFiche(PersonnePhysiqueRequest request) {
        Optional<PersonnePhysique> existante = rechercherFicheActiveExistante(
                request.getMatricule(), request.getNom(), request.getPrenoms(), request.getDateNaissance());
        if (existante.isPresent()) {
            throw new FicheDejaExistanteException(
                    "Cette personne est déjà inscrite au registre officiel. Utilisez la fiche existante pour y ajouter une infraction.",
                    existante.get().getId());
        }

        PersonnePhysique entity = ppMapper.toEntity(request);
        return ppMapper.toResponse(this.creer(entity));
    }

    @Override
    @Transactional
    public PersonnePhysiqueResponse modifierFiche(UUID id, PersonnePhysiqueRequest request) {
        PersonnePhysique existante = this.consulter(id);

        if ("ACTIVE".equals(existante.getStatutFiche())) {
            throw new IllegalStateException(
                    "Cette fiche est déjà active dans le registre officiel. Les informations d'identité ne sont plus modifiables ; seules les infractions peuvent être ajoutées ou mises à jour sur cette fiche.");
        }

        ppMapper.updateEntityFromRequest(request, existante);
        return ppMapper.toResponse(this.modifier(id, existante));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonnePhysiqueResponse obtenirFichePourAffichage(UUID id) {
        return ppMapper.toResponse(this.consulter(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonnePhysiquePublicResponse obtenirFichePourAffichagePublic(UUID id) {
        PersonnePhysique fiche = this.consulter(id);
        if (!"ACTIVE".equals(fiche.getStatutFiche())) {
            throw new ResourceNotFoundException("Fiche PP introuvable : " + id);
        }
        return ppMapper.toPublicResponse(fiche);
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationExistenceResponse verifierExistence(String matricule, String nom, String prenoms, LocalDate dateNaissance) {
        Optional<PersonnePhysique> existante = rechercherFicheActiveExistante(matricule, nom, prenoms, dateNaissance);
        if (existante.isPresent()) {
            return VerificationExistenceResponse.builder()
                    .existeDejaDansRegistre(true)
                    .ficheExistanteId(existante.get().getId())
                    .message("Cette personne est déjà inscrite au registre officiel. Vous pouvez uniquement ajouter une infraction sur sa fiche existante.")
                    .build();
        }
        return VerificationExistenceResponse.builder()
                .existeDejaDansRegistre(false)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonnePhysiqueResponse> rechercherMesFiches(String userId, String statut, Pageable pageable) {
        return ppRepo.rechercheMesFiches(userId, statut, pageable).map(ppMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheMiseEnCauseResponse> rechercherRegistreOfficiel(
            String recherche, UUID regionId, UUID entiteId, Pageable pageable) {
        return ficheMiseEnCauseServiceImpl.rechercherRegistreOfficiel(recherche, regionId, entiteId, pageable);
    }

    private Optional<PersonnePhysique> rechercherFicheActiveExistante(String matricule, String nom, String prenoms, LocalDate dateNaissance) {
        if (matricule != null && !matricule.isBlank()) {
            Optional<PersonnePhysique> parMatricule = ppRepo.findActiveByMatricule(matricule);
            if (parMatricule.isPresent()) {
                return parMatricule;
            }
        }
        if (nom != null && !nom.isBlank() && prenoms != null && !prenoms.isBlank() && dateNaissance != null) {
            return ppRepo.findActiveByIdentite(nom, prenoms, dateNaissance);
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public PersonnePhysiqueResponse soumettreFiche(UUID id, String agentId) {
        return ppMapper.toResponse(this.soumettre(id, agentId));
    }

    @Override
    @Transactional
    public PersonnePhysiqueResponse validerFiche(UUID id, String validateurId) {
        return ppMapper.toResponse(this.valider(id, validateurId));
    }

    @Override
    @Transactional
    public PersonnePhysiqueResponse rejeterFiche(UUID id, String motif, String validateurId) {
        return ppMapper.toResponse(this.rejeter(id, motif, validateurId));
    }

    @Override
    @Transactional
    public PersonnePhysiqueResponse modifierStatutJudiciaireFiche(UUID id, StatutJudiciaireRequest request, String agentId) {
        return ppMapper.toResponse(this.modifierStatutJudiciaire(id, request, agentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonnePhysiqueResponse> rechercherFichesInterne(String nom, UUID entiteId, UUID regionId, String statut, Pageable pageable) {
        return ppRepo.rechercheAvancee(nom, entiteId, regionId, statut, pageable).map(ppMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonnePhysiquePublicResponse> rechercherFichesPublic(String nom, UUID entiteId, UUID regionId, String statut, Pageable pageable) {
        return ppRepo.rechercheAvancee(nom, entiteId, regionId, statut, pageable).map(ppMapper::toPublicResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonnePhysique> listerRecentesBrouillonsOuSoumises(String userId, int limite) {
        return ppRepo.findRecentesByCreateur(userId, PageRequest.of(0, limite));
    }

    @Override
    @Transactional(readOnly = true)
    public FicheMiseEnCause consulterAvecDetails(UUID id) {
        FicheMiseEnCause fiche = consulter(id);

        if (fiche.getInfractions() != null) {
            fiche.getInfractions().size();
        }
        if (fiche.getPiecesJointes() != null) {
            fiche.getPiecesJointes().size();
        }

        return fiche;
    }

    @Override
    @Transactional(readOnly = true)
    public PersonnePhysique consulter(UUID id) {
        return ppRepo.findWithRelationsById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fiche PP introuvable : " + id));
    }

    @Override
    @Transactional
    public PersonnePhysique creer(PersonnePhysique pp) {
        pp.setStatutFiche("BROUILLON");
        hydraterReferences(pp);
        return ppRepo.save(pp);
    }

    @Override
    @Transactional
    public PersonnePhysique modifier(UUID id, PersonnePhysique pp) {
        hydraterReferences(pp);
        return ppRepo.save(pp);
    }

    // MODIFIÉ : délègue au service générique — voir FicheMiseEnCauseServiceImpl
    // pour la garde de transition désormais appliquée uniformément à PP et PM.
    @Override
    @Transactional
    public PersonnePhysique soumettre(UUID id, String agentId) {
        return (PersonnePhysique) ficheMiseEnCauseServiceImpl.soumettre(id, agentId);
    }

    // Reste spécifique : seule opération où PP porte une règle propre
    // (anti-doublon sur matricule / nom+prénoms+date de naissance).
    @Override
    @Transactional
    public PersonnePhysique valider(UUID id, String validateurId) {
        PersonnePhysique f = consulter(id);

        Optional<PersonnePhysique> doublon = rechercherFicheActiveExistante(
                f.getMatricule(), f.getNom(), f.getPrenoms(), f.getDateNaissance());
        if (doublon.isPresent() && !doublon.get().getId().equals(f.getId())) {
            throw new FicheDejaExistanteException(
                    "Impossible de valider cette fiche : une fiche active existe déjà pour cette identité dans le registre officiel.",
                    doublon.get().getId());
        }

        f.valider(validateurId);
        return ppRepo.save(f);
    }

    // MODIFIÉ : délègue au service générique
    @Override
    @Transactional
    public PersonnePhysique rejeter(UUID id, String motif, String validateurId) {
        return (PersonnePhysique) ficheMiseEnCauseServiceImpl.rejeter(id, motif, validateurId);
    }

    // MODIFIÉ : délègue au service générique
    @Override
    @Transactional
    public PersonnePhysique archiver(UUID id) {
        return (PersonnePhysique) ficheMiseEnCauseServiceImpl.archiver(id);
    }

    // MODIFIÉ : délègue au service générique, qui crée désormais
    // systématiquement une ligne d'historique (auparavant absent côté PP).
    @Override
    @Transactional
    public PersonnePhysique modifierStatutJudiciaire(UUID id, StatutJudiciaireRequest request, String agentId) {
        return (PersonnePhysique) ficheMiseEnCauseServiceImpl.modifierStatutJudiciaire(id, request, agentId);
    }

    @Override
    @Transactional
    public FicheMiseEnCause supprimerBrouillonOuSoumise(UUID id, String userId, boolean isAdmin) {
        return ficheMiseEnCauseServiceImpl.supprimerBrouillonOuSoumise(id, userId, isAdmin);
    }

    private void hydraterReferences(PersonnePhysique pp) {
        if (pp.getRegion() != null && pp.getRegion().getId() != null) {
            pp.setRegion(regionRepo.getReferenceById(pp.getRegion().getId()));
        }
        if (pp.getEntite() != null && pp.getEntite().getId() != null) {
            pp.setEntite(entiteRepo.getReferenceById(pp.getEntite().getId()));
        }
    }

    @Override @Transactional(readOnly = true) public long countEnAttente() { return ppRepo.countByStatutFiche("EN_ATTENTE_VALIDATION"); }
    @Override @Transactional(readOnly = true) public List<PersonnePhysique> listerTout() { return ppRepo.findAll(); }
}