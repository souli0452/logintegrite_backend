package bf.gov.ascelc.logintegrite_backend.referentiel.service;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.EntiteOrganisationRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.EntiteOrganisationResponse;

import java.util.List;
import java.util.UUID;

public interface EntiteOrganisationService {
    List<EntiteOrganisationResponse> lister();
    EntiteOrganisationResponse creer(EntiteOrganisationRequest request);
    EntiteOrganisationResponse modifier(UUID id, EntiteOrganisationRequest request);
    void supprimer(UUID id);
}
