// dossier/service/ImplicationFaitService.java
package bf.gov.ascelc.logintegrite_backend.dossier.service;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.ImplicationFaitRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.request.MiseAJourStatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.ImplicationFaitResponse;

import java.util.List;
import java.util.UUID;

public interface ImplicationFaitService {
    List<ImplicationFaitResponse> listerParImplication(UUID implicationId);
    ImplicationFaitResponse creer(UUID implicationId, ImplicationFaitRequest request);
    ImplicationFaitResponse mettreAJourStatut(UUID implicationFaitId, MiseAJourStatutJudiciaireRequest request);
}
