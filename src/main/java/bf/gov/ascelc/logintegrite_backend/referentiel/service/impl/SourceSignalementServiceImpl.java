package bf.gov.ascelc.logintegrite_backend.referentiel.service.impl;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.SourceSignalementRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.SourceSignalementResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.SourceSignalement;
import bf.gov.ascelc.logintegrite_backend.referentiel.mapper.SourceSignalementMapper;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.SourceSignalementRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.SourceSignalementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SourceSignalementServiceImpl implements SourceSignalementService {

    private final SourceSignalementRepository repository;
    private final SourceSignalementMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<SourceSignalementResponse> lister() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public SourceSignalementResponse creer(SourceSignalementRequest request) {
        SourceSignalement entite = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public SourceSignalementResponse modifier(UUID id, SourceSignalementRequest request) {
        SourceSignalement entite = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Source de signalement", id));
        mapper.mettreAJour(entite, request);
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public void supprimer(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Source de signalement", id);
        }
        repository.deleteById(id);
    }
}
