// document/service/DocumentService.java
package bf.gov.ascelc.logintegrite_backend.document.service;

import bf.gov.ascelc.logintegrite_backend.document.dto.request.DocumentRequest;
import bf.gov.ascelc.logintegrite_backend.document.dto.response.DocumentResponse;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.UUID;

public interface DocumentService {
    List<DocumentResponse> listerParDossier(UUID dossierId);
    DocumentResponse deposer(UUID dossierId, DocumentRequest request);
    DocumentResponse obtenir(UUID documentId);
    Resource telechargerFichier(UUID documentId);
    void tagger(UUID documentId, UUID implicationId);
    void retirerTag(UUID documentId, UUID implicationId);
    List<DocumentResponse> listerVisiblesPourImplication(UUID implicationId);
}
