package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.FicheMiseEnCauseResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonneMoraleResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonneMoralePublicResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.VerificationExistenceResponse;
import bf.gov.ascelc.logintegrite_backend.exception.FicheDejaExistanteException;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.repository.PersonneMoraleRepository;
import bf.gov.ascelc.logintegrite_backend.repository.RegionRepository;
import bf.gov.ascelc.logintegrite_backend.repository.EntiteOrganisationRepository;
import bf.gov.ascelc.logintegrite_backend.service.PersonneMoraleService;
import bf.gov.ascelc.logintegrite_backend.mapper.PersonneMoraleMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonneMoraleServiceImpl implements PersonneMoraleService {

    private final PersonneMoraleRepository pmRepo;
    private final RegionRepository regionRepo;
    private final EntiteOrganisationRepository entiteRepo;
    private final PersonneMoraleMapper pmMapper;
    private final FicheMiseEnCauseServiceImpl ficheMiseEnCauseServiceImpl;

    @Override
    @Transactional
    public PersonneMoraleResponse creerFiche(PersonneMoraleRequest request) {
        Optional<PersonneMorale> existante = rechercherFicheActiveExistante(request.getIfu(), request.getRaisonSociale());
        if (existante.isPresent()) {
            throw new FicheDejaExistanteException(
                    "Cette structure est déjà inscrite au registre officiel. Utilisez la fiche existante pour y ajouter une infraction.",
                    existante.get().getId());
        }

        PersonneMorale entity = pmMapper.toEntity(request);
        return pmMapper.toResponse(this.creer(entity));
    }

    @Override
    @Transactional
    public PersonneMoraleResponse modifierFiche(UUID id, PersonneMoraleRequest request) {
        PersonneMorale existante = this.consulter(id);

        if ("ACTIVE".equals(existante.getStatutFiche())) {
            throw new IllegalStateException(
                    "Cette fiche est déjà active dans le registre officiel. Les informations d'identité ne sont plus modifiables ; seules les infractions peuvent être ajoutées ou mises à jour sur cette fiche.");
        }

        pmMapper.updateEntityFromRequest(request, existante);
        return pmMapper.toResponse(this.modifier(id, existante));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonneMoraleResponse obtenirFichePourAffichage(UUID id) {
        return pmMapper.toResponse(this.consulter(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonneMoralePublicResponse obtenirFichePourAffichagePublic(UUID id) {
        PersonneMorale fiche = this.consulter(id);
        if (!"ACTIVE".equals(fiche.getStatutFiche())) {
            throw new ResourceNotFoundException("Fiche Personne Morale introuvable pour l'ID : " + id);
        }
        return pmMapper.toPublicResponse(fiche);
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationExistenceResponse verifierExistence(String ifu, String raisonSociale) {
        Optional<PersonneMorale> existante = rechercherFicheActiveExistante(ifu, raisonSociale);
        if (existante.isPresent()) {
            return VerificationExistenceResponse.builder()
                    .existeDejaDansRegistre(true)
                    .ficheExistanteId(existante.get().getId())
                    .message("Cette structure est déjà inscrite au registre officiel. Vous pouvez uniquement ajouter une infraction sur sa fiche existante.")
                    .build();
        }
        return VerificationExistenceResponse.builder()
                .existeDejaDansRegistre(false)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonneMoraleResponse> rechercherMesFiches(String userId, String statut, Pageable pageable) {
        return pmRepo.rechercheMesFiches(userId, statut, pageable).map(pmMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonneMorale> listerRecentesBrouillonsOuSoumises(String userId, int limite) {
        return pmRepo.findRecentesByCreateur(userId, PageRequest.of(0, limite));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheMiseEnCauseResponse> rechercherRegistreOfficiel(
            String recherche, UUID regionId, UUID entiteId, Pageable pageable) {
        return ficheMiseEnCauseServiceImpl.rechercherRegistreOfficiel(recherche, regionId, entiteId, pageable);
    }

    private Optional<PersonneMorale> rechercherFicheActiveExistante(String ifu, String raisonSociale) {
        if (ifu != null && !ifu.isBlank()) {
            Optional<PersonneMorale> parIfu = pmRepo.findActiveByIfu(ifu);
            if (parIfu.isPresent()) {
                return parIfu;
            }
        }
        if (raisonSociale != null && !raisonSociale.isBlank()) {
            return pmRepo.findActiveByRaisonSociale(raisonSociale);
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public PersonneMoraleResponse soumettreFiche(UUID id, String agentId) {
        return pmMapper.toResponse(this.soumettre(id, agentId));
    }

    @Override
    @Transactional
    public PersonneMoraleResponse validerFiche(UUID id, String validateurId) {
        return pmMapper.toResponse(this.valider(id, validateurId));
    }

    @Override
    @Transactional
    public PersonneMoraleResponse rejeterFiche(UUID id, String motif, String validateurId) {
        return pmMapper.toResponse(this.rejeter(id, motif, validateurId));
    }

    @Override
    @Transactional
    public PersonneMoraleResponse modifierStatutJudiciaireFiche(UUID id, StatutJudiciaireRequest request, String agentId) {
        return pmMapper.toResponse(this.modifierStatutJudiciaire(id, request, agentId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonneMoraleResponse> rechercherFichesInterne(String raisonSociale, UUID entiteId, UUID regionId, String statut, String typeStructure, Pageable pageable) {
        return pmRepo.rechercheAvancee(raisonSociale, entiteId, regionId, statut, typeStructure, pageable).map(pmMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PersonneMoralePublicResponse> rechercherFichesPublic(String raisonSociale, UUID entiteId, UUID regionId, String statut, String typeStructure, Pageable pageable) {
        return pmRepo.rechercheAvancee(raisonSociale, entiteId, regionId, statut, typeStructure, pageable).map(pmMapper::toPublicResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FicheMiseEnCauseResponse> rechercherFileAttenteValidation(UUID regionId, UUID entiteId, Pageable pageable) {
        return ficheMiseEnCauseServiceImpl.rechercherFileAttenteValidation(regionId, entiteId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonneMorale> listerRecentesValideesOuRejeteesParValidateur(String validateurId, int limite) {
        return pmRepo.findRecentesByValidateur(validateurId, PageRequest.of(0, limite));
    }

    @Override
    @Transactional(readOnly = true)
    public long compterDecisionsParValidateur(String validateurId, String statutFiche) {
        // CORRIGÉ : Utilisation de pmRepo à la place de ppRepo
        return pmRepo.countByValidateurIdAndStatutFiche(validateurId, statutFiche);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonneMorale> listerDecisionsParValidateur(String validateurId, String statutFiche) {
        return pmRepo.findByValidateurIdAndStatutFiche(validateurId, statutFiche, Pageable.unpaged());
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
        if (fiche.getHistoriqueStatuts() != null) {
            fiche.getHistoriqueStatuts().size();
        }

        return fiche;
    }

    @Override
    @Transactional(readOnly = true)
    public PersonneMorale consulter(UUID id) {
        return pmRepo.findWithRelationsById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fiche Personne Morale introuvable pour l'ID : " + id));
    }

    @Override
    @Transactional
    public PersonneMorale creer(PersonneMorale pm) {
        pm.setStatutFiche("BROUILLON");
        hydraterReferences(pm);
        return pmRepo.save(pm);
    }

    @Override
    @Transactional
    public PersonneMorale modifier(UUID id, PersonneMorale pm) {
        hydraterReferences(pm);
        return pmRepo.save(pm);
    }

    @Override
    @Transactional
    public PersonneMorale soumettre(UUID id, String agentId) {
        return (PersonneMorale) ficheMiseEnCauseServiceImpl.soumettre(id, agentId);
    }

    @Override
    @Transactional
    public PersonneMorale valider(UUID id, String validateurId) {
        PersonneMorale f = consulter(id);

        Optional<PersonneMorale> doublon = rechercherFicheActiveExistante(f.getIfu(), f.getRaisonSociale());
        if (doublon.isPresent() && !doublon.get().getId().equals(f.getId())) {
            throw new FicheDejaExistanteException(
                    "Impossible de valider cette fiche : une fiche active existe déjà pour cette structure dans le registre officiel.",
                    doublon.get().getId());
        }

        f.valider(validateurId);
        return pmRepo.save(f);
    }

    @Override
    @Transactional
    public PersonneMorale rejeter(UUID id, String motif, String validateurId) {
        return (PersonneMorale) ficheMiseEnCauseServiceImpl.rejeter(id, motif, validateurId);
    }

    @Override
    @Transactional
    public PersonneMorale archiver(UUID id) {
        return (PersonneMorale) ficheMiseEnCauseServiceImpl.archiver(id);
    }

    @Override
    @Transactional
    public PersonneMorale modifierStatutJudiciaire(UUID id, StatutJudiciaireRequest request, String agentId) {
        return (PersonneMorale) ficheMiseEnCauseServiceImpl.modifierStatutJudiciaire(id, request, agentId);
    }

    @Override
    @Transactional
    public FicheMiseEnCause supprimerBrouillonOuSoumise(UUID id, String userId, boolean isAdmin) {
        return ficheMiseEnCauseServiceImpl.supprimerBrouillonOuSoumise(id, userId, isAdmin);
    }

    private void hydraterReferences(PersonneMorale pm) {
        if (pm.getRegion() != null && pm.getRegion().getId() != null) {
            pm.setRegion(regionRepo.getReferenceById(pm.getRegion().getId()));
        }
        if (pm.getEntite() != null && pm.getEntite().getId() != null) {
            pm.setEntite(entiteRepo.getReferenceById(pm.getEntite().getId()));
        }
    }

    @Override @Transactional(readOnly = true) public long countEnAttente() { return pmRepo.countByStatutFiche("EN_ATTENTE_VALIDATION"); }
    @Override @Transactional(readOnly = true) public List<PersonneMorale> listerTout() { return pmRepo.findAll(); }
}