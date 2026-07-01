package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonneMoraleResponse;
import bf.gov.ascelc.logintegrite_backend.dto.response.PersonneMoralePublicResponse;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.repository.PersonneMoraleRepository;
import bf.gov.ascelc.logintegrite_backend.repository.RegionRepository;
import bf.gov.ascelc.logintegrite_backend.repository.EntiteOrganisationRepository;
import bf.gov.ascelc.logintegrite_backend.service.PersonneMoraleService;
import bf.gov.ascelc.logintegrite_backend.mapper.PersonneMoraleMapper;
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
public class PersonneMoraleServiceImpl implements PersonneMoraleService {

    private final PersonneMoraleRepository pmRepo;
    private final RegionRepository regionRepo;
    private final EntiteOrganisationRepository entiteRepo;
    private final PersonneMoraleMapper pmMapper;

    @Override
    @Transactional
    public PersonneMoraleResponse creerFiche(PersonneMoraleRequest request) {
        PersonneMorale entity = pmMapper.toEntity(request);
        return pmMapper.toResponse(this.creer(entity));
    }

    @Override
    @Transactional
    public PersonneMoraleResponse modifierFiche(UUID id, PersonneMoraleRequest request) {
        PersonneMorale existante = this.consulter(id);
        pmMapper.updateEntityFromRequest(request, existante);
        return pmMapper.toResponse(this.modifier(id, existante));
    }

    @Override
    @Transactional(readOnly = true)
    public PersonneMoraleResponse obtenirFichePourAffichage(UUID id) {
        return pmMapper.toResponse(this.consulter(id));
    }

    // AJOUT : version publique, même logique que côté PP. Seules les fiches ACTIVE
    // sont consultables, et seuls les champs de PersonneMoralePublicResponse sortent.
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
        PersonneMorale f = consulter(id);
        f.setStatutFiche("EN_ATTENTE_VALIDATION");
        return pmRepo.save(f);
    }

    @Override
    @Transactional
    public PersonneMorale valider(UUID id, String validateurId) {
        PersonneMorale f = consulter(id);
        f.setStatutFiche("ACTIVE");
        return pmRepo.save(f);
    }

    @Override
    @Transactional
    public PersonneMorale rejeter(UUID id, String motif, String validateurId) {
        PersonneMorale f = consulter(id);
        f.setStatutFiche("REJETE");
        f.setMotifRejet(motif);
        return pmRepo.save(f);
    }

    @Override
    @Transactional
    public PersonneMorale archiver(UUID id) {
        PersonneMorale f = consulter(id);
        f.setStatutFiche("ARCHIVE");
        return pmRepo.save(f);
    }

    @Override
    @Transactional
    public PersonneMorale modifierStatutJudiciaire(UUID id, StatutJudiciaireRequest request, String agentId) {
        PersonneMorale f = consulter(id);
        f.setStatutJudiciaire(request.getStatutJudiciaire());
        return pmRepo.save(f);
    }

    private void hydraterReferences(PersonneMorale pm) {
        // Utilisation de getReferenceById pour des liaisons d'ID ultra-rapides
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
