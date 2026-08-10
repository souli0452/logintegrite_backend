// personne/service/PersonneMoraleService.java
package bf.gov.ascelc.logintegrite_backend.personne.service;

import bf.gov.ascelc.logintegrite_backend.personne.dto.request.PersonneMoraleRequest;
import bf.gov.ascelc.logintegrite_backend.personne.dto.response.PersonneMoraleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PersonneMoraleService {
    Page<PersonneMoraleResponse> lister(Pageable pageable);
    PersonneMoraleResponse obtenir(UUID id);
    PersonneMoraleResponse creer(PersonneMoraleRequest request);
    PersonneMoraleResponse modifier(UUID id, PersonneMoraleRequest request);
    void supprimer(UUID id);
}
