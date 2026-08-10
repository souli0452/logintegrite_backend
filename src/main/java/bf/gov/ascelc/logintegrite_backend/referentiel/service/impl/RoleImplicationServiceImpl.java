package bf.gov.ascelc.logintegrite_backend.referentiel.service.impl;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.RoleImplicationRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.RoleImplicationResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.RoleImplication;
import bf.gov.ascelc.logintegrite_backend.referentiel.mapper.RoleImplicationMapper;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.RoleImplicationRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.RoleImplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleImplicationServiceImpl implements RoleImplicationService {

    private final RoleImplicationRepository repository;
    private final RoleImplicationMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<RoleImplicationResponse> lister() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public RoleImplicationResponse creer(RoleImplicationRequest request) {
        RoleImplication entite = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public RoleImplicationResponse modifier(UUID id, RoleImplicationRequest request) {
        RoleImplication entite = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role d'implication", id));
        mapper.mettreAJour(entite, request);
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public void supprimer(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Role d'implication", id);
        }
        repository.deleteById(id);
    }
}
