package bf.gov.ascelc.logintegrite_backend.referentiel.service.impl;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.TypeInfractionRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.TypeInfractionResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.CategorieInfraction;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.TypeInfraction;
import bf.gov.ascelc.logintegrite_backend.referentiel.mapper.TypeInfractionMapper;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.CategorieInfractionRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.TypeInfractionRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.TypeInfractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TypeInfractionServiceImpl implements TypeInfractionService {

    private final TypeInfractionRepository repository;
    private final CategorieInfractionRepository categorieInfractionRepository;
    private final TypeInfractionMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<TypeInfractionResponse> lister() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TypeInfractionResponse obtenir(UUID id) {
        return mapper.toResponse(trouverOuLever(id));
    }

    @Override
    public TypeInfractionResponse creer(TypeInfractionRequest request) {
        TypeInfraction entite = mapper.toEntity(request);
        entite.setCategorieInfraction(trouverCategorieOuLever(request.getCategorieInfractionId()));
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public TypeInfractionResponse modifier(UUID id, TypeInfractionRequest request) {
        TypeInfraction entite = trouverOuLever(id);
        entite.setLibelle(request.getLibelle());
        entite.setActif(request.isActif());
        entite.setCategorieInfraction(trouverCategorieOuLever(request.getCategorieInfractionId()));
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public void supprimer(UUID id) {
        repository.delete(trouverOuLever(id));
    }

    private TypeInfraction trouverOuLever(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Type d'infraction", id));
    }

    private CategorieInfraction trouverCategorieOuLever(UUID id) {
        return categorieInfractionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie d'infraction", id));
    }
}
