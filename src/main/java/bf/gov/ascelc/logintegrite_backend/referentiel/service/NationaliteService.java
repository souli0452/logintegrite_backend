package bf.gov.ascelc.logintegrite_backend.referentiel.service;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.NationaliteRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.NationaliteResponse;

import java.util.List;
import java.util.UUID;

public interface NationaliteService {
    List<NationaliteResponse> lister();
    List<NationaliteResponse> listerActifs();
    NationaliteResponse obtenir(UUID id);
    NationaliteResponse creer(NationaliteRequest request);
    NationaliteResponse modifier(UUID id, NationaliteRequest request);
    void supprimer(UUID id);
}
