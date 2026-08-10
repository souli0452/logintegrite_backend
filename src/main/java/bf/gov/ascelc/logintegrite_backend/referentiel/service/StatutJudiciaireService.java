package bf.gov.ascelc.logintegrite_backend.referentiel.service;

import bf.gov.ascelc.logintegrite_backend.referentiel.dto.request.StatutJudiciaireRequest;
import bf.gov.ascelc.logintegrite_backend.referentiel.dto.response.StatutJudiciaireResponse;

import java.util.List;
import java.util.UUID;

public interface StatutJudiciaireService {
    List<StatutJudiciaireResponse> lister();
    StatutJudiciaireResponse creer(StatutJudiciaireRequest request);
    StatutJudiciaireResponse modifier(UUID id, StatutJudiciaireRequest request);
    void supprimer(UUID id);
}
