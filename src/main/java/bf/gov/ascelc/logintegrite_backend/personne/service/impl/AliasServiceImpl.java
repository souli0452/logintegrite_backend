package bf.gov.ascelc.logintegrite_backend.personne.service.impl;

import bf.gov.ascelc.logintegrite_backend.audit.service.AuditService;
import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.personne.dto.request.AliasRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.AliasResponse;
import bf.gov.ascelc.logintegrite_backend.personne.entity.Alias;
import bf.gov.ascelc.logintegrite_backend.personne.entity.Personne;
import bf.gov.ascelc.logintegrite_backend.personne.mapper.AliasMapper;
import bf.gov.ascelc.logintegrite_backend.personne.repository.AliasRepository;
import bf.gov.ascelc.logintegrite_backend.personne.repository.PersonneRepository;
import bf.gov.ascelc.logintegrite_backend.personne.service.AliasService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AliasServiceImpl implements AliasService {

    private final AliasRepository repository;
    private final PersonneRepository personneRepository;
    private final AliasMapper mapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public List<AliasResponse> listerParPersonne(UUID personneId) {
        Personne personne = trouverPersonneOuLever(personneId);
        return personne.getAliases().stream().map(mapper::toResponse).toList();
    }

    @Override
    public AliasResponse creer(UUID personneId, AliasRequest request) {
        Personne personne = trouverPersonneOuLever(personneId);

        Alias alias = new Alias();
        alias.setPersonne(personne);
        alias.setNomAlias(request.getNomAlias());
        alias.setCommentaire(request.getCommentaire());
        Alias sauvegarde = repository.save(alias);

        auditService.enregistrer("CREATION", "Alias", sauvegarde.getId(), null,
                Map.of("nomAlias", sauvegarde.getNomAlias(), "personneId", personneId.toString()));

        return mapper.toResponse(sauvegarde);
    }

    @Override
    public void supprimer(UUID aliasId) {
        Alias alias = repository.findById(aliasId)
                .orElseThrow(() -> new ResourceNotFoundException("Alias", aliasId));
        auditService.enregistrer("SUPPRESSION", "Alias", aliasId,
                Map.of("nomAlias", alias.getNomAlias()), null);
        repository.delete(alias);
    }

    private Personne trouverPersonneOuLever(UUID id) {
        return personneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Personne", id));
    }
}
