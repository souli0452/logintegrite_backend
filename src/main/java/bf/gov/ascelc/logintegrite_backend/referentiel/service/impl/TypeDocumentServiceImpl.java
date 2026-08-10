package bf.gov.ascelc.logintegrite_backend.referentiel.service.impl;

import bf.gov.ascelc.logintegrite_backend.common.exception.ResourceNotFoundException;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.TypeDocumentRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.TypeDocumentResponse;
import bf.gov.ascelc.logintegrite_backend.referentiel.entity.TypeDocument;
import bf.gov.ascelc.logintegrite_backend.referentiel.mapper.TypeDocumentMapper;
import bf.gov.ascelc.logintegrite_backend.referentiel.repository.TypeDocumentRepository;
import bf.gov.ascelc.logintegrite_backend.referentiel.service.TypeDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TypeDocumentServiceImpl implements TypeDocumentService {

    private final TypeDocumentRepository repository;
    private final TypeDocumentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<TypeDocumentResponse> lister() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public TypeDocumentResponse creer(TypeDocumentRequest request) {
        TypeDocument entite = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public TypeDocumentResponse modifier(UUID id, TypeDocumentRequest request) {
        TypeDocument entite = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Type de document", id));
        mapper.mettreAJour(entite, request);
        return mapper.toResponse(repository.save(entite));
    }

    @Override
    public void supprimer(UUID id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Type de document", id);
        }
        repository.deleteById(id);
    }
}
