package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.request.RechercheSauvegardeeRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.RechercheSauvegardeeResponse;
import java.util.List;
import java.util.UUID;

public interface RechercheSauvegardeeService {

    RechercheSauvegardeeResponse create(RechercheSauvegardeeRequest request);

    // AJOUT du paramètre userId : avant, seul getMySearches filtrait par utilisateur.
    // update/getById/delete ne vérifiaient pas le propriétaire (IDOR).
    RechercheSauvegardeeResponse update(UUID id, RechercheSauvegardeeRequest request, String userId);

    RechercheSauvegardeeResponse getById(UUID id, String userId);

    List<RechercheSauvegardeeResponse> getMySearches(String userId);

    void delete(UUID id, String userId);
}
