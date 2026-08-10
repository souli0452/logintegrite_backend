// dossier/service/impl/DossierServiceImpl.java (complet, allege)
package bf.gov.ascelc.logintegrite_backend.dossier.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.audit.service.ConsultationService;
import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.DossierRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.DossierResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Dossier;
import bf.gov.ascelc.logintegrite_backend.dossier.mapper.DossierMapper;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.DossierRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.service.DossierService;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.SourceSignalementRepository;
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
public class DossierServiceImpl implements DossierService {

    private final DossierRepository repository;
    private final SourceSignalementRepository sourceSignalementRepository;
    private final DossierMapper mapper;
    private final AuditService auditService;
    private final ConsultationService consultationService;

    @Override
    @Transactional(readOnly = true)
    public Page<DossierResponse> lister(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public DossierResponse obtenir(UUID id) {
        Dossier entite = trouverOuLever(id);
        consultationService.enregistrer("Dossier", id);
        return mapper.toResponse(entite);
    }

    @Override
    public DossierResponse creer(DossierRequest request) {
        Dossier entite = mapper.toEntity(request);
        entite.setSourceSignalement(sourceSignalementRepository.findById(request.getSourceSignalementId())
                .orElseThrow(() -> new ResourceNotFoundException("Source de signalement", request.getSourceSignalementId())));
        Dossier sauvegarde = repository.save(entite);

        auditService.enregistrer("CREATION", "Dossier", sauvegarde.getId(), null,
                Map.of("intitule", String.valueOf(sauvegarde.getIntitule()),
                       "statutDossier", sauvegarde.getStatutDossier().name()));

        return mapper.toResponse(sauvegarde);
    }

    @Override
    public DossierResponse modifier(UUID id, DossierRequest request) {
        Dossier entite = trouverOuLever(id);
        Map<String, Object> avant = Map.of(
                "intitule", String.valueOf(entite.getIntitule()),
                "statutDossier", entite.getStatutDossier().name());

        mapper.updateEntityFromRequest(request, entite);
        entite.setSourceSignalement(sourceSignalementRepository.findById(request.getSourceSignalementId())
                .orElseThrow(() -> new ResourceNotFoundException("Source de signalement", request.getSourceSignalementId())));
        Dossier sauvegarde = repository.save(entite);

        auditService.enregistrer("MODIFICATION", "Dossier", sauvegarde.getId(), avant,
                Map.of("intitule", String.valueOf(sauvegarde.getIntitule()),
                       "statutDossier", sauvegarde.getStatutDossier().name()));

        return mapper.toResponse(sauvegarde);
    }

    private Dossier trouverOuLever(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Dossier", id));
    }
}
