package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.request.EntiteOrganisationRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.EntiteOrganisationResponse;
import java.util.List;
import java.util.UUID; 

public interface EntiteOrganisationService {
    EntiteOrganisationResponse create(EntiteOrganisationRequest request);
    EntiteOrganisationResponse update(UUID id, EntiteOrganisationRequest request); 
    EntiteOrganisationResponse getById(UUID id);
    List<EntiteOrganisationResponse> getAll();
    List<EntiteOrganisationResponse> getAllActifs();
    void delete(UUID id); 
}
