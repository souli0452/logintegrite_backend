// dossier/service/DossierService.java (complet)
package bf.gov.ascelc.logintegrite_backend.dossier.service;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.DossierRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.DossierResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

// ouvrirDossier() retiree d'ici : elle vit maintenant sur DossierWorkflowService.
public interface DossierService {
    Page<DossierResponse> lister(Pageable pageable);
    DossierResponse obtenir(UUID id);
    DossierResponse creer(DossierRequest request);
    DossierResponse modifier(UUID id, DossierRequest request);
}
