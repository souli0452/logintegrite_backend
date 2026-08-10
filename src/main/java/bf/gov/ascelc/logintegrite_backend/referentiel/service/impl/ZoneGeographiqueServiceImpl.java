package bf.gov.ascelc.logintegrite_backend.referentiel.service.impl;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.ZoneGeographiqueRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.ZoneGeographiqueResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.ZoneGeographique;
import bf.gov.ascelc.logintegrite_backend.referentiel.mapper.ZoneGeographiqueMapper;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.ZoneGeographiqueRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.ZoneGeographiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ZoneGeographiqueServiceImpl implements ZoneGeographiqueService {

    private final ZoneGeographiqueRepository repository;
    private final ZoneGeographiqueMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ZoneGeographiqueResponse> lister() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public ZoneGeographiqueResponse creer(ZoneGeographiqueRequest request) {
        ZoneGeographique entite = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public ZoneGeographiqueResponse modifier(UUID id, ZoneGeographiqueRequest request) {
        ZoneGeographique entite = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone geographique", id));

        // Un noeud ne peut pas etre son propre parent (evite les cycles simples)
        if (request.getParentId() != null && request.getParentId().equals(id)) {
            throw new IllegalArgumentException("Une zone ne peut pas etre son propre parent");
        }

        mapper.mettreAJour(entite, request);
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public void supprimer(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Zone geographique", id);
        }
        repository.deleteById(id);
    }
}
