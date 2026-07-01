package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonnePhysiqueResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonnePhysiquePublicResponse;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.repository.PersonnePhysiqueRepository;
import bf.gov.ascelc.logintegrite_backend.repository.RegionRepository;
import bf.gov.ascelc.logintegrite_backend.repository.EntiteOrganisationRepository;
import bf.gov.ascelc.logintegrite_backend.service.PersonnePhysiqueService;
import bf.gov.ascelc.logintegrite_backend.mapper.PersonnePhysiqueMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonnePhysiqueServiceImpl implements PersonnePhysiqueService {

    private final PersonnePhysiqueRepository ppRepo;
    private final RegionRepository regionRepo;
    private final EntiteOrganisationRepository entiteRepo;
    private final PersonnePhysiqueMapper ppMapper;

    @Override
    @Transactional
    public PersonnePhysiqueResponse creerFiche(PersonnePhysiqueRequest request) {
        PersonnePhysique entity = ppMapper.toEntity(request);
        return ppMapper.toResponse(this.creer(entity));
    }

    @Override
    @Transactional
    public PersonnePhysiqueResponse modifierFiche(UUID id, PersonnePhysiqueRequest request) {
        PersonnePhysique existante = this.consulter(id);
        ppMapper.updateEntityFromRequest(request, existante);
        return ppMapper.toResponse(this.modifier(id, existante));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonnePhysiqueResponse obtenirFichePourAffichage(UUID id) {
        return ppMapper.toResponse(this.consulter(id));
    }

    // AJOUT : version publique. Seules les fiches ACTIVE sont consultables,
    // et seuls les champs de PersonnePhysiquePublicResponse sont renvoyés
    // (pas de matricule, date de naissance, etc.)
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
    public FicheMiseEnCause consulterAvecDetails(UUID id) {
        FicheMiseEnCause fiche = consulter(id); // Réutilise leur logique de consultation existante

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

    @Override
    @Transactional
    public PersonnePhysique soumettre(UUID id, String agentId) {
        PersonnePhysique f = consulter(id);
        f.setStatutFiche("EN_ATTENTE_VALIDATION");
        return ppRepo.save(f);
    }

    @Override
    @Transactional
    public PersonnePhysique valider(UUID id, String validateurId) {
        PersonnePhysique f = consulter(id);
        f.setStatutFiche("ACTIVE");
        return ppRepo.save(f);
    }

    @Override
    @Transactional
    public PersonnePhysique rejeter(UUID id, String motif, String validateurId) {
        PersonnePhysique f = consulter(id);
        f.setStatutFiche("REJETE");
        f.setMotifRejet(motif);
        return ppRepo.save(f);
    }

    @Override
    @Transactional
    public PersonnePhysique archiver(UUID id) {
        PersonnePhysique f = consulter(id);
        f.setStatutFiche("ARCHIVE");
        return ppRepo.save(f);
    }

    @Override
    @Transactional
    public PersonnePhysique modifierStatutJudiciaire(UUID id, StatutJudiciaireRequest request, String agentId) {
        PersonnePhysique f = consulter(id);
        f.setStatutJudiciaire(request.getStatutJudiciaire());
        return ppRepo.save(f);
    }

    private void hydraterReferences(PersonnePhysique pp) {
        // Utilisation de getReferenceById pour éviter les SELECT superflus
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
