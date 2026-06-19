package bf.gov.ascelc.logintegrite_backend.service;

import bf.gov.ascelc.logintegrite_backend.entity.PersonnePhysique;
import java.util.List;
import java.util.UUID;

public interface PersonnePhysiqueService extends FicheMiseEnCauseService {
    PersonnePhysique creer(PersonnePhysique pp);
    PersonnePhysique modifier(UUID id, PersonnePhysique pp);
    List<PersonnePhysique> listerTout();
    PersonnePhysique consulter(UUID id);
}