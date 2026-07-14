package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireReferentielRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.StatutJudiciaireReferentielResponse;
import bf.gov.ascelc.logintegrite_backend.entity.StatutJudiciaireReferentiel;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.mapper.StatutJudiciaireReferentielMapper;
import bf.gov.ascelc.logintegrite_backend.repository.StatutJudiciaireReferentielRepository;
import bf.gov.ascelc.logintegrite_backend.service.StatutJudiciaireReferentielService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StatutJudiciaireReferentielServiceImpl implements StatutJudiciaireReferentielService {

    private final StatutJudiciaireReferentielRepository repository;
    private final StatutJudiciaireReferentielMapper mapper;

    @Override
    public StatutJudiciaireReferentielResponse create(StatutJudiciaireReferentielRequest request) {
        StatutJudiciaireReferentiel entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public StatutJudiciaireReferentielResponse update(UUID id, StatutJudiciaireReferentielRequest request) {
        StatutJudiciaireReferentiel entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Statut judiciaire non trouvé avec l'id : " + id));
        mapper.updateEntityFromRequest(request, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public StatutJudiciaireReferentielResponse getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Statut judiciaire non trouvé avec l'id : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatutJudiciaireReferentielResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatutJudiciaireReferentielResponse> getAllActifs() {
        return repository.findByActifTrue().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }


@Override
public void delete(UUID id) {
    StatutJudiciaireReferentiel entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Statut judiciaire non trouvé avec l'id : " + id));
    entity.setActif(false);
    repository.save(entity);
}
}
