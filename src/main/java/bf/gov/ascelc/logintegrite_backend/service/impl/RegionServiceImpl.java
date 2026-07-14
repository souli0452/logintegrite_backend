package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.dto.request.RegionRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.RegionResponse;
import bf.gov.ascelc.logintegrite_backend.entity.Region;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.mapper.RegionMapper;
import bf.gov.ascelc.logintegrite_backend.repository.RegionRepository;
import bf.gov.ascelc.logintegrite_backend.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RegionServiceImpl implements RegionService {

    private final RegionRepository repository;
    private final RegionMapper mapper;

    @Override
    public RegionResponse create(RegionRequest request) {
        Region entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public RegionResponse update(UUID id, RegionRequest request) {
        Region entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Région non trouvée avec l'id : " + id));
        mapper.updateEntityFromRequest(request, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public RegionResponse getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Région non trouvée avec l'id : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegionResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
@Transactional(readOnly = true)
public List<RegionResponse> getAllActifs() {
    return repository.findByActifTrue().stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
}

    @Override
public void delete(UUID id) {
    Region entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Région non trouvée avec l'id : " + id));
    entity.setActif(false);
    repository.save(entity);
}
}
