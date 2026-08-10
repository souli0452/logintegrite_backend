package bf.gov.ascelc.logintegrite_backend.dossier.service;

import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.ImplicationFaitResumeResponse;
import bf.gov.ascelc.logintegrite_backend.dossier.dto.response.PersonneDossierCompletResponse;

import java.util.List;
import java.util.UUID;

public interface PersonneDossierQueryService {
    PersonneDossierCompletResponse obtenirPourPersonne(UUID personneId);
    List<ImplicationFaitResumeResponse> listerImplicationFaitsPourPersonne(UUID personneId);
}
