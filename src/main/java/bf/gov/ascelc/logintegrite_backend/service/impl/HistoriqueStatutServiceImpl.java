package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.dto.request.HistoriqueStatutRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.HistoriqueStatutResponse;
import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.entity.HistoriqueStatut;
import bf.gov.ascelc.logintegrite_backend.entity.Infraction;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.mapper.HistoriqueStatutMapper;
import bf.gov.ascelc.logintegrite_backend.repository.FicheMiseEnCauseRepository;
import bf.gov.ascelc.logintegrite_backend.repository.HistoriqueStatutRepository;
import bf.gov.ascelc.logintegrite_backend.repository.InfractionRepository;
import bf.gov.ascelc.logintegrite_backend.service.HistoriqueStatutService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HistoriqueStatutServiceImpl implements HistoriqueStatutService {

    private final HistoriqueStatutRepository repository;
    private final FicheMiseEnCauseRepository ficheRepository;
    private final InfractionRepository infractionRepository; // AJOUT
    private final HistoriqueStatutMapper mapper;

    @Override
    public HistoriqueStatutResponse create(HistoriqueStatutRequest request) {
        FicheMiseEnCause fiche = ficheRepository.findById(request.getFicheId())
                .orElseThrow(() -> new ResourceNotFoundException("Fiche de mise en cause non trouvée avec l'id : " + request.getFicheId()));

        HistoriqueStatut entity = mapper.toEntity(request);
        entity.setFiche(fiche);


        if (request.getInfractionId() != null) {
            Infraction infraction = infractionRepository.findById(request.getInfractionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Infraction non trouvée avec l'id : " + request.getInfractionId()));
            if (infraction.getFiche() == null || !infraction.getFiche().getId().equals(fiche.getId())) {
                throw new IllegalStateException("L'infraction sélectionnée n'appartient pas à cette fiche.");
            }
            entity.setInfraction(infraction);
        }

        fiche.setStatutJudiciaire(request.getNouveauStatut());
        ficheRepository.save(fiche);

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public HistoriqueStatutResponse getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Ligne d'historique non trouvée avec l'id : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriqueStatutResponse> getByFicheId(UUID ficheId) {
        return repository.findByFicheIdOrderByCreatedAtDesc(ficheId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriqueStatutResponse> getByInfractionId(UUID infractionId) {
        return repository.findByInfractionIdOrderByCreatedAtDesc(infractionId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}