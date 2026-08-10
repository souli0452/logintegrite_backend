package bf.gov.ascelc.logintegrite_backend.referentiel.service.impl;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.StatutJudiciaireResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.StatutJudiciaire;
import bf.gov.ascelc.logintegrite_backend.referentiel.mapper.StatutJudiciaireMapper;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.StatutJudiciaireRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.StatutJudiciaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StatutJudiciaireServiceImpl implements StatutJudiciaireService {

    private final StatutJudiciaireRepository repository;
    private final StatutJudiciaireMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<StatutJudiciaireResponse> lister() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public StatutJudiciaireResponse creer(StatutJudiciaireRequest request) {
        StatutJudiciaire entite = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public StatutJudiciaireResponse modifier(UUID id, StatutJudiciaireRequest request) {
        StatutJudiciaire entite = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Statut judiciaire", id));
        mapper.mettreAJour(entite, request);
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public void supprimer(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Statut judiciaire", id);
        }
        repository.deleteById(id);
    }
}
