package bf.gov.ascelc.logintegrite_backend.referentiel.service.impl;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.TypePieceIdentiteRefRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.TypePieceIdentiteRefResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.TypePieceIdentiteRef;
import bf.gov.ascelc.logintegrite_backend.referentiel.mapper.TypePieceIdentiteRefMapper;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.TypePieceIdentiteRefRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.TypePieceIdentiteRefService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TypePieceIdentiteRefServiceImpl implements TypePieceIdentiteRefService {

    private final TypePieceIdentiteRefRepository repository;
    private final TypePieceIdentiteRefMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<TypePieceIdentiteRefResponse> lister() {
        return mapper.toResponseList(repository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TypePieceIdentiteRefResponse> listerActifs() {
        return mapper.toResponseList(repository.findByActifTrueOrderByLibelleAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public TypePieceIdentiteRefResponse obtenir(UUID id) {
        return mapper.toResponse(
            repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type de pièce d'identité", id))
        );
    }

    @Override
    public TypePieceIdentiteRefResponse creer(TypePieceIdentiteRefRequest request) {
        if (repository.existsByCodeIgnoreCase(request.getCode())) {
            throw new IllegalArgumentException("Un type avec ce code existe déjà : " + request.getCode());
        }
        TypePieceIdentiteRef entite = mapper.toEntity(request);
        entite.setCode(request.getCode().toUpperCase().trim());
        if (request.getActif() == null) {
            entite.setActif(true);
        }
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public TypePieceIdentiteRefResponse modifier(UUID id, TypePieceIdentiteRefRequest request) {
        TypePieceIdentiteRef entite = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Type de pièce d'identité", id));
        // Le code est immuable — mapper.updateEntity l'ignore automatiquement
        mapper.updateEntity(request, entite);
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public void supprimer(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Type de pièce d'identité", id);
        }
        try {
            repository.deleteById(id);
        } catch (Exception e) {
            throw new IllegalStateException(
                "Impossible de supprimer ce type : il est utilisé par des pièces d'identité existantes. Préférez la désactivation.");
        }
    }
}
