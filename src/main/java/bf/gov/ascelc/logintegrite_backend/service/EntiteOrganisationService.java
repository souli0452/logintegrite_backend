package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.request.EntiteOrganisationRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.EntiteOrganisationResponse;
import java.util.List;
import java.util.UUID; // Ajout de l'import

public interface EntiteOrganisationService {
    EntiteOrganisationResponse create(EntiteOrganisationRequest request);
    EntiteOrganisationResponse update(UUID id, EntiteOrganisationRequest request); // Changé en UUID
    EntiteOrganisationResponse getById(UUID id); // Changé en UUID
    List<EntiteOrganisationResponse> getAll();
    void delete(UUID id); // Changé en UUID
}