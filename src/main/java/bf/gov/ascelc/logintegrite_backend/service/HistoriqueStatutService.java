package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.request.HistoriqueStatutRequest;
import bf.gov.ascelc.logintegrite_backend.dto.response.HistoriqueStatutResponse;
import java.util.List;
import java.util.UUID;

public interface HistoriqueStatutService {
    HistoriqueStatutResponse create(HistoriqueStatutRequest request);
    HistoriqueStatutResponse getById(UUID id);
    List<HistoriqueStatutResponse> getByFicheId(UUID ficheId);

    // AJOUT
    List<HistoriqueStatutResponse> getByInfractionId(UUID infractionId);
}