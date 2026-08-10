// securite/service/impl/RoleHabilitationServiceImpl.java
package bf.gov.ascelc.logintegrite_backend.securite.service.impl;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.securite.dto.request.RoleHabilitationRequest;
import bf.gov.ascelc.logintegrite_backend.securite.dto.response.RoleHabilitationResponse;
import bf.gov.ascelc.logintegrite_backend.securite.entity.RoleHabilitation;
import bf.gov.ascelc.logintegrite_backend.securite.mapper.RoleHabilitationMapper;
import bf.gov.ascelc.logintegrite_backend.securite.repository.RoleHabilitationRepository;
import bf.gov.ascelc.logintegrite_backend.securite.service.RoleHabilitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleHabilitationServiceImpl implements RoleHabilitationService {

    private final RoleHabilitationRepository repository;
    private final RoleHabilitationMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleHabilitationResponse> lister() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public RoleHabilitationResponse creer(RoleHabilitationRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Override
    public RoleHabilitationResponse modifier(UUID id, RoleHabilitationRequest request) {
        RoleHabilitation entite = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role habilitation", id));
        mapper.updateEntityFromRequest(request, entite);
        return mapper.toResponse(repository.save(entite));
    }
}
