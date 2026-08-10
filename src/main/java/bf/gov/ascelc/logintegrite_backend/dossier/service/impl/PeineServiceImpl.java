// dossier/service/impl/PeineServiceImpl.java (complet)
package bf.gov.ascelc.logintegrite_backend.dossier.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.PeineRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.PeineResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.ImplicationFait;
import bf.gov.ascelc.logintegrite_backend.dossier.entity.Peine;
import bf.gov.ascelc.logintegrite_backend.dossier.mapper.PeineMapper;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.ImplicationFaitRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.repository.PeineRepository;
import bf.gov.ascelc.logintegrite_backend.dossier.service.PeineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PeineServiceImpl implements PeineService {

    private final PeineRepository repository;
    private final ImplicationFaitRepository implicationFaitRepository;
    private final PeineMapper mapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<PeineResponse> listerParImplicationFait(UUID implicationFaitId) {
        return repository.findByImplicationFaitId(implicationFaitId).stream().map(mapper::toResponse).toList();
    }

    @Override
    public PeineResponse creer(UUID implicationFaitId, PeineRequest request) {
        ImplicationFait implicationFait = implicationFaitRepository.findById(implicationFaitId)
                .orElseThrow(() -> new ResourceNotFoundException("Lien implication-fait", implicationFaitId));

        Peine entite = mapper.toEntity(request);
        entite.setImplicationFait(implicationFait);
        Peine sauvegarde = repository.save(entite);

        auditService.enregistrer("CREATION", "Peine", sauvegarde.getId(), null,
                Map.of("implicationFaitId", implicationFait.getId().toString(),
                       "typePeine", sauvegarde.getTypePeine().name()));

        return mapper.toResponse(sauvegarde);
    }
}
