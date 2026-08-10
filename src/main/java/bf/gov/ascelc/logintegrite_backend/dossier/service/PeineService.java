// dossier/service/PeineService.java
package bf.gov.ascelc.logintegrite_backend.dossier.service;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.PeineRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.PeineResponse;

import java.util.List;
import java.util.UUID;

public interface PeineService {
    List<PeineResponse> listerParImplicationFait(UUID implicationFaitId);
    PeineResponse creer(UUID implicationFaitId, PeineRequest request);
}
