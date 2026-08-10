package bf.gov.ascelc.logintegrite_backend.personne.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.audit.service.ConsultationService;
import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.common.security.CurrentUserProvider;
import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.PersonnePhysiqueResponse;
import bf.gov.ascelc.logintegrite_backend.personne.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.personne.mapper.PersonnePhysiqueMapper;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PersonnePhysiqueRepository;
import bf.gov.ascelc.logintegrite_backend.personne.service.PersonnePhysiqueService;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.Nationalite;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.NationaliteRepository;
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
public class PersonnePhysiqueServiceImpl implements PersonnePhysiqueService {

    private final PersonnePhysiqueRepository repository;
    private final NationaliteRepository nationaliteRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PersonnePhysiqueMapper mapper;
    private final AuditService auditService;
    private final ConsultationService consultationService;

    @Override
    @Transactional(readOnly = true)
    public Page<PersonnePhysiqueResponse> lister(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonnePhysiqueResponse obtenir(UUID id) {
        PersonnePhysique entite = trouverOuLever(id);
        consultationService.enregistrer("PersonnePhysique", id);
        return mapper.toResponse(entite);
    }

    @Override
    public PersonnePhysiqueResponse creer(PersonnePhysiqueRequest request) {
        PersonnePhysique entite = mapper.toEntity(request);
        entite.setCreePar(currentUserProvider.utilisateurCourant());

        // Résolution de la relation Nationalite
        if (request.getNationaliteId() != null) {
            Nationalite nat = nationaliteRepository.findById(request.getNationaliteId())
                .orElseThrow(() -> new ResourceNotFoundException("Nationalité", request.getNationaliteId()));
            entite.setNationaliteRef(nat);
            entite.setNationalite(nat.getLibelle());
        }

        PersonnePhysique sauvegarde = repository.save(entite);

        auditService.enregistrer("CREATION", "PersonnePhysique", sauvegarde.getId(), null,
                Map.of("nomNaissance", sauvegarde.getNomNaissance(),
                       "prenoms", sauvegarde.getPrenoms(),
                       "nationalite", String.valueOf(sauvegarde.getNationalite())));

        return mapper.toResponse(sauvegarde);
    }

    @Override
    public PersonnePhysiqueResponse modifier(UUID id, PersonnePhysiqueRequest request) {
        PersonnePhysique entite = trouverOuLever(id);
        Map<String, Object> avant = Map.of(
                "nomUsage", String.valueOf(entite.getNomUsage()),
                "adresse", String.valueOf(entite.getAdresse()),
                "telephone", String.valueOf(entite.getTelephone()));

        mapper.updateEntityFromRequest(request, entite);

        // Résolution de la relation Nationalite
        if (request.getNationaliteId() != null) {
            Nationalite nat = nationaliteRepository.findById(request.getNationaliteId())
                .orElseThrow(() -> new ResourceNotFoundException("Nationalité", request.getNationaliteId()));
            entite.setNationaliteRef(nat);
            entite.setNationalite(nat.getLibelle());
        }

        PersonnePhysique sauvegarde = repository.save(entite);

        auditService.enregistrer("MODIFICATION", "PersonnePhysique", sauvegarde.getId(), avant,
                Map.of("nomUsage", String.valueOf(sauvegarde.getNomUsage()),
                       "adresse", String.valueOf(sauvegarde.getAdresse()),
                       "telephone", String.valueOf(sauvegarde.getTelephone())));

        return mapper.toResponse(sauvegarde);
    }

    @Override
    public void supprimer(UUID id) {
        PersonnePhysique entite = trouverOuLever(id);
        auditService.enregistrer("SUPPRESSION", "PersonnePhysique", id,
                Map.of("nomAffichage", entite.getNomAffichage()), null);
        repository.delete(entite);
    }

    private PersonnePhysique trouverOuLever(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personne physique", id));
    }
}
