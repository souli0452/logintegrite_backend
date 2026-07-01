package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireReferentielRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.StatutJudiciaireReferentielResponse;
import java.util.List;
import java.util.UUID;

public interface StatutJudiciaireReferentielService {
    StatutJudiciaireReferentielResponse create(StatutJudiciaireReferentielRequest request);
    StatutJudiciaireReferentielResponse update(UUID id, StatutJudiciaireReferentielRequest request);
    StatutJudiciaireReferentielResponse getById(UUID id);
    List<StatutJudiciaireReferentielResponse> getAll();
    List<StatutJudiciaireReferentielResponse> getAllActifs();
    void delete(UUID id);
}