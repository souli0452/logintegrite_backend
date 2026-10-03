// dossier/service/DossierService.java (complet)
package bf.gov.ascelc.logintegrite_backend.dossier.service;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.DossierRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.DossierResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.enums.StatutDossier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

// ouvrirDossier() retiree d'ici : elle vit maintenant sur DossierWorkflowService.
public interface DossierService {

    /**
     * Cloture un dossier ouvert : il devient non modifiable. Refuse tant qu'un fait attend une validation.
     */
    DossierResponse cloturer(UUID id);

    Page<DossierResponse> lister(String recherche, StatutDossier statut, Pageable pageable);
    DossierResponse obtenir(UUID id);
    DossierResponse creer(DossierRequest request);
    DossierResponse modifier(UUID id, DossierRequest request);
}
