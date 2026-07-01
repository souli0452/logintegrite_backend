package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.dto.request.InfractionRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.InfractionResponse;
import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.entity.Infraction;
import bf.gov.ascelc.logintegrite_backend.entity.TypeInfraction;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.mapper.InfractionMapper;
import bf.gov.ascelc.logintegrite_backend.mapper.PieceJointeMapper;
import bf.gov.ascelc.logintegrite_backend.mapper.HistoriqueStatutMapper;
import bf.gov.ascelc.logintegrite_backend.repository.FicheMiseEnCauseRepository;
import bf.gov.ascelc.logintegrite_backend.repository.InfractionRepository;
import bf.gov.ascelc.logintegrite_backend.repository.PieceJointeRepository;
import bf.gov.ascelc.logintegrite_backend.repository.HistoriqueStatutRepository;
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
    private final PieceJointeRepository pieceJointeRepository; // AJOUT
    private final HistoriqueStatutRepository historiqueStatutRepository; // AJOUT
    private final InfractionMapper mapper;
    private final PieceJointeMapper pieceJointeMapper; // AJOUT
    private final HistoriqueStatutMapper historiqueStatutMapper; // AJOUT

    @Override
    public InfractionResponse create(InfractionRequest request) {
        Infraction entity = mapper.toEntity(request);

        FicheMiseEnCause fiche = ficheRepository.findById(request.getFicheId())
                .orElseThrow(() -> new ResourceNotFoundException("Fiche de mise en cause non trouvée avec l'id : " + request.getFicheId()));
        entity.setFiche(fiche);

        if (request.getTypeInfractionId() != null) {
            TypeInfraction type = typeInfractionRepository.findById(request.getTypeInfractionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Type d'infraction non trouvé avec l'id : " + request.getTypeInfractionId()));
            entity.setTypeInfraction(type);
        }

        return enrichir(mapper.toResponse(repository.save(entity)));
    }

    @Override
    public InfractionResponse update(UUID id, InfractionRequest request) {
        Infraction entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Infraction non trouvée avec l'id : " + id));

        mapper.updateEntityFromRequest(request, entity);

        if (!entity.getFiche().getId().equals(request.getFicheId())) {
            FicheMiseEnCause fiche = ficheRepository.findById(request.getFicheId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fiche de mise en cause non trouvée avec l'id : " + request.getFicheId()));
            entity.setFiche(fiche);
        }

        if (request.getTypeInfractionId() != null) {
            TypeInfraction type = typeInfractionRepository.findById(request.getTypeInfractionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Type d'infraction non trouvé avec l'id : " + request.getTypeInfractionId()));
            entity.setTypeInfraction(type);
        } else {
            entity.setTypeInfraction(null);
        }

        return enrichir(mapper.toResponse(repository.save(entity)));
    }

    @Override
    @Transactional(readOnly = true)
    public InfractionResponse getById(UUID id) {
        InfractionResponse response = repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Infraction non trouvée avec l'id : " + id));
        return enrichir(response);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InfractionResponse> getAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .map(this::enrichir)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InfractionResponse> getByFicheId(UUID ficheId) {
        return repository.findByFicheId(ficheId).stream()
                .map(mapper::toResponse)
                .map(this::enrichir)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Infraction non trouvée avec l'id : " + id);
        }
        repository.deleteById(id);
    }

    // AJOUT : complète la réponse avec pièces jointes + historique propres à
    // l'infraction, en un seul appel réseau pour l'écran Angular.
    private InfractionResponse enrichir(InfractionResponse response) {
        response.setPiecesJointes(
                pieceJointeRepository.findByInfractionId(response.getId()).stream()
                        .map(pieceJointeMapper::toResponse)
                        .collect(Collectors.toList())
        );
        response.setHistoriqueStatuts(
                historiqueStatutRepository.findByInfractionIdOrderByCreatedAtDesc(response.getId()).stream()
                        .map(historiqueStatutMapper::toResponse)
                        .collect(Collectors.toList())
        );
        return response;
    }
}