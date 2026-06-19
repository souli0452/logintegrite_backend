package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.dto.request.HistoriqueStatutRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.HistoriqueStatutResponse;
import bf.gov.ascelc.logintegrite_backend.abstracts.FicheMiseEnCause;
import bf.gov.ascelc.logintegrite_backend.entity.HistoriqueStatut;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.mapper.HistoriqueStatutMapper;
import bf.gov.ascelc.logintegrite_backend.repository.FicheMiseEnCauseRepository;
import bf.gov.ascelc.logintegrite_backend.repository.HistoriqueStatutRepository;
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
    private final HistoriqueStatutMapper mapper;

    @Override
    public HistoriqueStatutResponse create(HistoriqueStatutRequest request) {
        // 1. Récupération de la fiche (qu'elle soit PersonnePhysique ou PersonneMorale sous le capot)
        FicheMiseEnCause fiche = ficheRepository.findById(request.getFicheId())
                .orElseThrow(() -> new ResourceNotFoundException("Fiche de mise en cause non trouvée avec l'id : " + request.getFicheId()));

        // 2. Conversion du DTO en Entité historique via MapStruct
        HistoriqueStatut entity = mapper.toEntity(request);
        entity.setFiche(fiche);

        // 3. Maintien de la cohérence : on applique le nouveau statut au champ 'statutJudiciaire'
        fiche.setStatutJudiciaire(request.getNouveauStatut());

        // 4. On sauvegarde la fiche pour persister son nouvel état en base de données
        ficheRepository.save(fiche);

        // 5. On enregistre la ligne d'historique et on retourne la réponse
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
}