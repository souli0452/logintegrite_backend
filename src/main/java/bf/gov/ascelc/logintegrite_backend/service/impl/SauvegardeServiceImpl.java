package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.dto.request.SauvegardeRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.SauvegardeResponse;
import bf.gov.ascelc.logintegrite_backend.entity.Sauvegarde;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.mapper.SauvegardeMapper;
import bf.gov.ascelc.logintegrite_backend.repository.SauvegardeRepository;
import bf.gov.ascelc.logintegrite_backend.service.SauvegardeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SauvegardeServiceImpl implements SauvegardeService {

    private final SauvegardeRepository repository;
    private final SauvegardeMapper mapper;

    @Override
    public SauvegardeResponse registrarDebut(SauvegardeRequest request) {
        Sauvegarde entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public SauvegardeResponse registrarFin(UUID id, String statut, LocalDateTime dateFin) {
        Sauvegarde entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sauvegarde non trouvée avec l'id : " + id));

        entity.setStatut(statut);
        entity.setDateFin(dateFin);

        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SauvegardeResponse> getHistorique() {
        return repository.findAllByOrderByDateDebutDesc().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SauvegardeResponse getById(UUID id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Sauvegarde non trouvée avec l'id : " + id));
    }
}