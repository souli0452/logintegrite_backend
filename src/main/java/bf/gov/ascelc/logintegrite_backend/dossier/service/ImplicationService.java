// dossier/service/ImplicationService.java
package bf.gov.ascelc.logintegrite_backend.dossier.service;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.ImplicationRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.ImplicationResponse;

import java.util.List;
import java.util.UUID;

public interface ImplicationService {
    List<ImplicationResponse> listerParDossier(UUID dossierId);
    ImplicationResponse creer(UUID dossierId, ImplicationRequest request);
}
