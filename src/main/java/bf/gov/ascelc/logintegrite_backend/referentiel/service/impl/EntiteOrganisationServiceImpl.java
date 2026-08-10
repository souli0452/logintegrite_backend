package bf.gov.ascelc.logintegrite_backend.referentiel.service.impl;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.EntiteOrganisationRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.EntiteOrganisationResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.EntiteOrganisation;
import bf.gov.ascelc.logintegrite_backend.referentiel.mapper.EntiteOrganisationMapper;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.EntiteOrganisationRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.EntiteOrganisationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class EntiteOrganisationServiceImpl implements EntiteOrganisationService {

    private final EntiteOrganisationRepository repository;
    private final EntiteOrganisationMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<EntiteOrganisationResponse> lister() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public EntiteOrganisationResponse creer(EntiteOrganisationRequest request) {
        EntiteOrganisation entite = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public EntiteOrganisationResponse modifier(UUID id, EntiteOrganisationRequest request) {
        EntiteOrganisation entite = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entite d'organisation", id));

        // Un noeud ne peut pas etre son propre parent (evite les cycles simples)
        if (request.getParentId() != null && request.getParentId().equals(id)) {
            throw new IllegalArgumentException("Une entite ne peut pas etre son propre parent");
        }

        mapper.mettreAJour(entite, request);
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public void supprimer(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Entite d'organisation", id);
        }
        repository.deleteById(id);
    }
}
