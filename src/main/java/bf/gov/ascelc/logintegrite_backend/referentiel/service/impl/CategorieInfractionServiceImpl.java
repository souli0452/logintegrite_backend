package bf.gov.ascelc.logintegrite_backend.referentiel.service.impl;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.CategorieInfractionRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.CategorieInfractionResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.CategorieInfraction;
import bf.gov.ascelc.logintegrite_backend.referentiel.mapper.CategorieInfractionMapper;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.CategorieInfractionRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.CategorieInfractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CategorieInfractionServiceImpl implements CategorieInfractionService {

    private final CategorieInfractionRepository repository;
    private final CategorieInfractionMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<CategorieInfractionResponse> lister() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategorieInfractionResponse obtenir(UUID id) {
        return mapper.toResponse(trouverOuLever(id));
    }

    @Override
    public CategorieInfractionResponse creer(CategorieInfractionRequest request) {
        return mapper.toResponse(repository.save(mapper.toEntity(request)));
    }

    @Override
    public CategorieInfractionResponse modifier(UUID id, CategorieInfractionRequest request) {
        CategorieInfraction entite = trouverOuLever(id);
        mapper.updateEntityFromRequest(request, entite);
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public void supprimer(UUID id) {
        repository.delete(trouverOuLever(id));
    }

    private CategorieInfraction trouverOuLever(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Categorie d'infraction", id));
    }
}
