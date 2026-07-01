package bf.gov.ascelc.logintegrite_backend.service.impl;

import bf.gov.ascelc.logintegrite_backend.dto.request.RechercheSauvegardeeRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.RechercheSauvegardeeResponse;
import bf.gov.ascelc.logintegrite_backend.entity.RechercheSauvegardee;
import bf.gov.ascelc.logintegrite_backend.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.mapper.RechercheSauvegardeeMapper;
import bf.gov.ascelc.logintegrite_backend.repository.RechercheSauvegardeeRepository;
import bf.gov.ascelc.logintegrite_backend.service.RechercheSauvegardeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RechercheSauvegardeeServiceImpl implements RechercheSauvegardeeService {

    private final RechercheSauvegardeeRepository repository;
    private final RechercheSauvegardeeMapper mapper;

    @Override
    public RechercheSauvegardeeResponse create(RechercheSauvegardeeRequest request) {
        RechercheSauvegardee entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    public RechercheSauvegardeeResponse update(UUID id, RechercheSauvegardeeRequest request, String userId) {
        RechercheSauvegardee entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recherche sauvegardée non trouvée avec l'id : " + id));

        // Contrôle de propriété ajouté ici (IDOR)
        verifierProprietaire(entity, userId);

        mapper.updateEntityFromRequest(request, entity);
        return mapper.toResponse(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public RechercheSauvegardeeResponse getById(UUID id, String userId) {
        RechercheSauvegardee entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recherche sauvegardée non trouvée avec l'id : " + id));

        // Contrôle de propriété ajouté ici (IDOR)
        verifierProprietaire(entity, userId);

        return mapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RechercheSauvegardeeResponse> getMySearches(String userId) {
        return repository.findByCreatedByIdOrderByCreatedAtDesc(userId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id, String userId) {
        RechercheSauvegardee entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recherche sauvegardée non trouvée avec l'id : " + id));

        // Contrôle de propriété ajouté ici (IDOR)
        verifierProprietaire(entity, userId);

        repository.delete(entity);
    }

    // Vérifie que la recherche appartient bien à l'utilisateur courant
    private void verifierProprietaire(RechercheSauvegardee entity, String userId) {
        if (entity.getCreatedById() == null || !entity.getCreatedById().equals(userId)) {
            throw new AccessDeniedException(
                    "La recherche sauvegardée " + entity.getId() + " n'appartient pas à l'utilisateur courant.");
        }
    }
}
