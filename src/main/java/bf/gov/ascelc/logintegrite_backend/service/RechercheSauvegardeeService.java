package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.request.RechercheSauvegardeeRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.RechercheSauvegardeeResponse;
import java.util.List;
import java.util.UUID;

public interface RechercheSauvegardeeService {
    RechercheSauvegardeeResponse create(RechercheSauvegardeeRequest request);
    RechercheSauvegardeeResponse update(UUID id, RechercheSauvegardeeRequest request);
    RechercheSauvegardeeResponse getById(UUID id);
    List<RechercheSauvegardeeResponse> getMySearches(String userId);
    void delete(UUID id);
}