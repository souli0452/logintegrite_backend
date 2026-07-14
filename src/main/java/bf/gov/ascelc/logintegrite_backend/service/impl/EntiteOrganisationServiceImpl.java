package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.dto.request.EntiteOrganisationRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.EntiteOrganisationResponse;
import bf.gov.ascelc.logintegrite_backend.entity.EntiteOrganisation;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.mapper.EntiteOrganisationMapper;
import bf.gov.ascelc.logintegrite_backend.repository.EntiteOrganisationRepository;
import bf.gov.ascelc.logintegrite_backend.service.EntiteOrganisationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID; // Ajout de l'import
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EntiteOrganisationServiceImpl implements EntiteOrganisationService {

    private final EntiteOrganisationRepository repository;
    private final EntiteOrganisationMapper mapper;

    @Override
    public EntiteOrganisationResponse create(EntiteOrganisationRequest request) {
        EntiteOrganisation entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public EntiteOrganisationResponse update(UUID id, EntiteOrganisationRequest request) { // Changé en UUID
        EntiteOrganisation entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation non trouvée avec l'id : " + id));
        mapper.updateEntityFromRequest(request, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public EntiteOrganisationResponse getById(UUID id) { // Changé en UUID
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation non trouvée avec l'id : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntiteOrganisationResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
@Transactional(readOnly = true)
public List<EntiteOrganisationResponse> getAllActifs() {
    return repository.findByActifTrue().stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
}


@Override
public void delete(UUID id) {
    EntiteOrganisation entity = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Organisation non trouvée avec l'id : " + id));
    entity.setActif(false);
    repository.save(entity);
}
}
