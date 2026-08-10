package bf.gov.ascelc.logintegrite_backend.referentiel.service;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.CategorieInfractionRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.CategorieInfractionResponse;

import java.util.List;
import java.util.UUID;

public interface CategorieInfractionService {
    List<CategorieInfractionResponse> lister();
    CategorieInfractionResponse obtenir(UUID id);
    CategorieInfractionResponse creer(CategorieInfractionRequest request);
    CategorieInfractionResponse modifier(UUID id, CategorieInfractionRequest request);
    void supprimer(UUID id);
}
