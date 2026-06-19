package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.dto.request.InfractionRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.InfractionResponse;
import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.entity.Infraction;
import bf.gov.ascelc.logintegrite_backend.entity.TypeInfraction;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.mapper.InfractionMapper;
import bf.gov.ascelc.logintegrite_backend.repository.FicheMiseEnCauseRepository;
import bf.gov.ascelc.logintegrite_backend.repository.InfractionRepository;
import bf.gov.ascelc.logintegrite_backend.repository.TypeInfractionRepository;
import bf.gov.ascelc.logintegrite_backend.service.InfractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InfractionServiceImpl implements InfractionService {

    private final InfractionRepository repository;
    private final FicheMiseEnCauseRepository ficheRepository;
    private final TypeInfractionRepository typeInfractionRepository;
    private final InfractionMapper mapper;

    @Override
    public InfractionResponse create(InfractionRequest request) {
        Infraction entity = mapper.toEntity(request);

        // Résolution de la liaison obligatoire vers la fiche
        FicheMiseEnCause fiche = ficheRepository.findById(request.getFicheId())
                .orElseThrow(() -> new ResourceNotFoundException("Fiche de mise en cause non trouvée avec l'id : " + request.getFicheId()));
        entity.setFiche(fiche);

        // Résolution de la liaison optionnelle vers la typologie légale
        if (request.getTypeInfractionId() != null) {
            TypeInfraction type = typeInfractionRepository.findById(request.getTypeInfractionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Type d'infraction non trouvé avec l'id : " + request.getTypeInfractionId()));
            entity.setTypeInfraction(type);
        }

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public InfractionResponse update(UUID id, InfractionRequest request) {
        Infraction entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Infraction non trouvée avec l'id : " + id));

        mapper.updateEntityFromRequest(request, entity);

        // Re-vérification de la cohérence si la fiche change
        if (!entity.getFiche().getId().equals(request.getFicheId())) {
            FicheMiseEnCause fiche = ficheRepository.findById(request.getFicheId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fiche de mise en cause non trouvée avec l'id : " + request.getFicheId()));
            entity.setFiche(fiche);
        }

        // Mise à jour du type d'infraction
        if (request.getTypeInfractionId() != null) {
            TypeInfraction type = typeInfractionRepository.findById(request.getTypeInfractionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Type d'infraction non trouvé avec l'id : " + request.getTypeInfractionId()));
            entity.setTypeInfraction(type);
        } else {
            entity.setTypeInfraction(null);
        }

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public InfractionResponse getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Infraction non trouvée avec l'id : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InfractionResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InfractionResponse> getByFicheId(UUID ficheId) {
        // Utilisation directe de la requête personnalisée du repository (plus performant)
        return repository.findByFicheId(ficheId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Infraction non trouvée avec l'id : " + id);
        }
        repository.deleteById(id);
    }
}