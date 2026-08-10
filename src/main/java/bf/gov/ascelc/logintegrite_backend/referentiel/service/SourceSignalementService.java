package bf.gov.ascelc.logintegrite_backend.referentiel.service;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.SourceSignalementRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.SourceSignalementResponse;

import java.util.List;
import java.util.UUID;

public interface SourceSignalementService {
    List<SourceSignalementResponse> lister();
    SourceSignalementResponse creer(SourceSignalementRequest request);
    SourceSignalementResponse modifier(UUID id, SourceSignalementRequest request);
    void supprimer(UUID id);
}
