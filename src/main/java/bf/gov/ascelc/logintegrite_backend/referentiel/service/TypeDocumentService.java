package bf.gov.ascelc.logintegrite_backend.referentiel.service;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.TypeDocumentRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.TypeDocumentResponse;

import java.util.List;
import java.util.UUID;

public interface TypeDocumentService {
    List<TypeDocumentResponse> lister();
    TypeDocumentResponse creer(TypeDocumentRequest request);
    TypeDocumentResponse modifier(UUID id, TypeDocumentRequest request);
    void supprimer(UUID id);
}
