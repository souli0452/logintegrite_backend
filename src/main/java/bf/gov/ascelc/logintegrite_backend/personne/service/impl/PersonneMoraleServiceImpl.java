// personne/service/impl/PersonneMoraleServiceImpl.java (complet)
package bf.gov.ascelc.logintegrite_backend.personne.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.audit.service.ConsultationService;
import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.PersonneMoraleResponse;
import bf.gov.ascelc.logintegrite_backend.personne.entity.PersonneMorale;
import bf.gov.ascelc.logintegrite_backend.personne.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.personne.mapper.PersonneMoraleMapper;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PersonneMoraleRepository;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PersonnePhysiqueRepository;
import bf.gov.ascelc.logintegrite_backend.personne.service.PersonneMoraleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonneMoraleServiceImpl implements PersonneMoraleService {

    private final PersonneMoraleRepository repository;
    private final PersonnePhysiqueRepository personnePhysiqueRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PersonneMoraleMapper mapper;
    private final AuditService auditService;
    private final ConsultationService consultationService;

    @Override
    @Transactional(readOnly = true)
    public Page<PersonneMoraleResponse> lister(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonneMoraleResponse obtenir(UUID id) {
        PersonneMorale entite = trouverOuLever(id);
        consultationService.enregistrer("PersonneMorale", id);
        return mapper.toResponse(entite);
    }

    @Override
    public PersonneMoraleResponse creer(PersonneMoraleRequest request) {
        PersonneMorale entite = mapper.toEntity(request);
        entite.setCreePar(currentUserProvider.utilisateurCourant());
        if (request.getRepresentantLegalId() != null) {
            entite.setRepresentantLegal(trouverPersonnePhysiqueOuLever(request.getRepresentantLegalId()));
        }
        PersonneMorale sauvegarde = repository.save(entite);

        auditService.enregistrer("CREATION", "PersonneMorale", sauvegarde.getId(), null,
                Map.of("denominationSociale", sauvegarde.getDenominationSociale(),
                       "formeJuridique", sauvegarde.getFormeJuridique(),
                       "statut", sauvegarde.getStatut().name()));

        return mapper.toResponse(sauvegarde);
    }

    @Override
    public PersonneMoraleResponse modifier(UUID id, PersonneMoraleRequest request) {
        PersonneMorale entite = trouverOuLever(id);
        Map<String, Object> avant = Map.of(
                "statut", entite.getStatut().name(),
                "siegeSocial", String.valueOf(entite.getSiegeSocial()));

        mapper.updateEntityFromRequest(request, entite);
        if (request.getRepresentantLegalId() != null) {
            entite.setRepresentantLegal(trouverPersonnePhysiqueOuLever(request.getRepresentantLegalId()));
        }
        PersonneMorale sauvegarde = repository.save(entite);

        auditService.enregistrer("MODIFICATION", "PersonneMorale", sauvegarde.getId(), avant,
                Map.of("statut", sauvegarde.getStatut().name(),
                       "siegeSocial", String.valueOf(sauvegarde.getSiegeSocial())));

        return mapper.toResponse(sauvegarde);
    }

    @Override
    public void supprimer(UUID id) {
        PersonneMorale entite = trouverOuLever(id);
        auditService.enregistrer("SUPPRESSION", "PersonneMorale", id,
                Map.of("denominationSociale", entite.getDenominationSociale()), null);
        repository.delete(entite);
    }

    private PersonneMorale trouverOuLever(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personne morale", id));
    }

    private PersonnePhysique trouverPersonnePhysiqueOuLever(UUID id) {
        return personnePhysiqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personne physique (representant legal)", id));
    }
}
