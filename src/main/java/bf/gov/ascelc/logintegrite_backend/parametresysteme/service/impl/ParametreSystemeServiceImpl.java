package bf.gov.ascelc.logintegrite_backend.parametresysteme.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.parametresysteme.dto.request.ParametreSystemeRequest;
import bf.gov.ascelc.logintegrite_backend.parametresysteme.dto.response.ParametreSystemeResponse;
import bf.gov.ascelc.logintegrite_backend.parametresysteme.entity.ParametreSysteme;
import bf.gov.ascelc.logintegrite_backend.parametresysteme.mapper.ParametreSystemeMapper;
import bf.gov.ascelc.logintegrite_backend.parametresysteme.repository.ParametreSystemeRepository;
import bf.gov.ascelc.logintegrite_backend.parametresysteme.service.ParametreSystemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ParametreSystemeServiceImpl implements ParametreSystemeService {

    private final ParametreSystemeRepository repository;
    private final ParametreSystemeMapper mapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<ParametreSystemeResponse> lister() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ParametreSystemeResponse obtenir(UUID id) {
        return mapper.toResponse(trouverOuLever(id));
    }

    @Override
    public ParametreSystemeResponse creer(ParametreSystemeRequest request) {
        if (repository.existsByCle(request.getCle())) {
            throw new IllegalArgumentException("Un parametre avec la cle '" + request.getCle() + "' existe deja.");
        }
        ParametreSysteme entite = mapper.toEntity(request);
        ParametreSysteme sauvegarde = repository.save(entite);

        auditService.enregistrer("CREATION", "ParametreSysteme", sauvegarde.getId(), null,
                Map.of("cle", sauvegarde.getCle(), "valeur", String.valueOf(sauvegarde.getValeur())));

        return mapper.toResponse(sauvegarde);
    }

    @Override
    public ParametreSystemeResponse modifier(UUID id, ParametreSystemeRequest request) {
        ParametreSysteme entite = trouverOuLever(id);
        String valeurAvant = entite.getValeur();

        mapper.updateEntityFromRequest(request, entite);
        ParametreSysteme sauvegarde = repository.save(entite);

        auditService.enregistrer("MODIFICATION", "ParametreSysteme", sauvegarde.getId(),
                Map.of("valeur", String.valueOf(valeurAvant)),
                Map.of("valeur", String.valueOf(sauvegarde.getValeur())));

        return mapper.toResponse(sauvegarde);
    }

    @Override
    public void supprimer(UUID id) {
        ParametreSysteme entite = trouverOuLever(id);
        auditService.enregistrer("SUPPRESSION", "ParametreSysteme", id,
                Map.of("cle", entite.getCle()), null);
        repository.delete(entite);
    }

    private ParametreSysteme trouverOuLever(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parametre systeme", id));
    }
}
