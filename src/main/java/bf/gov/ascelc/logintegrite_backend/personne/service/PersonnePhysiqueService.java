// personne/service/PersonnePhysiqueService.java
package bf.gov.ascelc.logintegrite_backend.personne.service;

import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonnePhysiqueRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.PersonnePhysiqueResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PersonnePhysiqueService {
    Page<PersonnePhysiqueResponse> lister(Pageable pageable);
    PersonnePhysiqueResponse obtenir(UUID id);
    PersonnePhysiqueResponse creer(PersonnePhysiqueRequest request);
    PersonnePhysiqueResponse modifier(UUID id, PersonnePhysiqueRequest request);
    void supprimer(UUID id);
}
