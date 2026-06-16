package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import bf.gov.ascelc.logintegrite_backend.service.FicheMiseEnCauseService;
import java.util.UUID;

public interface PersonnePhysiqueService extends FicheMiseEnCauseService {
    PersonnePhysique creer(PersonnePhysique pp);
    PersonnePhysique modifier(UUID id, PersonnePhysique pp);
}