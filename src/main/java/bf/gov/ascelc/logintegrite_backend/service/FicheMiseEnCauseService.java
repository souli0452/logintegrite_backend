package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.entity.FicheMiseEnCause;
import java.util.UUID;

public interface FicheMiseEnCauseService {
    FicheMiseEnCause consulter(UUID id);
    FicheMiseEnCause soumettre(UUID id, String agentId);
    FicheMiseEnCause valider(UUID id, String validateurId);
    FicheMiseEnCause rejeter(UUID id, String motif, String validateurId);
    FicheMiseEnCause archiver(UUID id);
    FicheMiseEnCause modifierStatutJudiciaire(UUID id, StatutJudiciaireRequest request, String agentId);
    long countEnAttente();
}